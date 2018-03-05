/*****************************************************************************
**
**  Implementation of the dframetrackergpf_b1 interface
**
**  Creation date:  2014/12/16
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include    "frametrackergpf_b1.h"
#include    "boxing/unboxer/frametrackerutil.h"

//  BASE INCLUDES
//
#include    "boxing/log.h"
#include    "boxing/platform/memory.h"
#include    "areasampler.h"
#include    "2polinomialsampler.h"

//  SYSTEM INCLUDES
//
#include    <math.h>

//  DEFINES
//

#define SMEMBER(member) (((boxing_tracker_gpf_b1 *)tracker)->member)
#define BASEMEMBER(member) (((boxing_tracker_gpf *)tracker)->member)
#define BASEBASEMEMBER(member) (((boxing_tracker*)tracker)->member)

//  PRIVATE INTERFACE
//

static int   track_frame1(boxing_tracker * tracker, const boxing_image8 * frame);
static int   track_frame_analog_mode(boxing_tracker_gpf * tracker, const boxing_image8 * input_image);
static void  frame_tracker_gpf_b1_free(boxing_tracker * tracker);
static void  get_displacement_matrix(boxing_tracker_gpf_b1 * tracker, const boxing_image8 * image, boxing_matrixf * displacement_matrix);
static DBOOL calc_horizontal_offset(boxing_tracker_gpf_b1 * tracker,
                                 const boxing_image8 * image,
                                 const boxing_pointi from,
                                 const boxing_pointi to,
                                 boxing_float * offsets,
                                 int scan_direction);
static DBOOL calculate_reference_bars(boxing_tracker_gpf_b1 * tracker,
                                   const boxing_image8 * image,
                                   frame_corner_marks * corner_marks,
                                   frame_reference_bars * reference_bars);
static DBOOL calculate_intersection_points(const boxing_linef * horizontal_lines,
                                 const boxing_linef * vertical_lines,
                                 int lines_size_h,
                                 int lines_size_v,
                                 const boxing_float * offsets,
                                 const boxing_float vertical_barwidth,
                                 const boxing_float width,
                                 boxing_matrixf * intersections_points);
static DBOOL calculate_sampling_location(
                               boxing_tracker_gpf_b1 * tracker,
                               const boxing_pointf * left_bar_points,
                               const int left_bar_points_size,
                               const boxing_pointf * right_bar_points,
                               const int right_bar_points_size,
                               const boxing_pointf * top_bar_points,
                               const int top_bar_points_size,
                               const boxing_pointf * bottom_bar_points,
                               const int bottom_bar_points_size,
                               const boxing_float * left_offset_xs,
                               const boxing_float * right_offset_xs,
                               int offsets_size,
                               int    width,
                               int    vertical_barwidth,
                               const frame_corner_marks* corner_marks);

// PUBLIC FRAME TRACKER GPF B1 FUNCTIONS
//

boxing_tracker_gpf_b1 * boxing_frame_tracker_gpf_b1_create(boxing_frame_gpf_b1 * generic_frame)
{
    boxing_tracker_gpf_b1 * tracker = BOXING_MEMORY_ALLOCATE_TYPE(boxing_tracker_gpf_b1);
    boxing_frame_tracker_gpf_base_init(&tracker->base, (boxing_frame*)generic_frame);
    tracker->base.base.mode = BOXING_TRACK_HORIZONTAL_SHIFT|
                              BOXING_TRACK_CONTENT_CONTAINER|
                              BOXING_TRACK_METADATA_CONTAINER|
                              BOXING_TRACK_CALIBRATION_BAR|
                              BOXING_TRACK_VERTICAL_SHIFT;
    tracker->base.base.track_frame = track_frame1;
    tracker->base.base.free = frame_tracker_gpf_b1_free;
    tracker->base.track_frame_analog_mode = track_frame_analog_mode;
    tracker->frame_dimension = generic_frame->base.size(&generic_frame->base);
    tracker->contaner_dimension = generic_frame->base.container(&generic_frame->base)->dimension(generic_frame->base.container(&generic_frame->base));
    tracker->corner_mark_dimension = boxing_frame_gpf_b1_corner_mark_dimension(generic_frame);
    tracker->corner_mark_gap_size = boxing_frame_gpf_b1_corner_mark_gap_size(generic_frame);

    boxing_generic_container * container = tracker->base.generic_frame->metadata_container(tracker->base.generic_frame);
    boxing_pointi tile_size          = container->get_tile_size(container);
    boxing_pointi metadata_dimension = {container->dimension(container).x/tile_size.x,
            container->dimension(container).y/tile_size.y};

    container = tracker->base.generic_frame->container(tracker->base.generic_frame);
    boxing_pointi container_tile_size = container->get_tile_size(container);
    boxing_pointi contaner_dimension = {container->dimension(container).x/container_tile_size.x,
            container->dimension(container).y/container_tile_size.y};

    tracker->base.content_sampler          = boxing_2polinomialsampler_create(contaner_dimension.x, contaner_dimension.y);
    tracker->base.metadata_sampler         = boxing_areasampler_create(metadata_dimension.x, metadata_dimension.y);
    tracker->base.calibration_bar_sampler  = boxing_2polinomialsampler_create(1, 256);

    gvector_create_inplace(&tracker->reference_bars.top_reference_bar, sizeof(boxing_pointf), 0);
    gvector_create_inplace(&tracker->reference_bars.left_reference_bar, sizeof(boxing_pointf), 0);
    gvector_create_inplace(&tracker->reference_bars.bottom_reference_bar, sizeof(boxing_pointf), 0);
    gvector_create_inplace(&tracker->reference_bars.right_reference_bar, sizeof(boxing_pointf), 0);
    return tracker;
}

// PRIVATE FRAME TRACKER GPF B1 FUNCTIONS
//

static void frame_tracker_gpf_b1_free(boxing_tracker * tracker)
{
    boxing_memory_free(SMEMBER(reference_bars).top_reference_bar.buffer);
    boxing_memory_free(SMEMBER(reference_bars).left_reference_bar.buffer);
    boxing_memory_free(SMEMBER(reference_bars).bottom_reference_bar.buffer);
    boxing_memory_free(SMEMBER(reference_bars).right_reference_bar.buffer);

    boxing_frame_tracker_gpf_base_free(tracker);
}

static int track_frame1(boxing_tracker * tracker, const boxing_image8 * frame)
{
    int retval = boxing_frame_tracker_gpf_base_track_frame(tracker, frame);
    boxing_areasampler_set_radius((boxing_areasampler*)BASEMEMBER(metadata_sampler), (int)( (4/(boxing_float)2.4)*BASEMEMBER(base.x_sampling_rate) ));
    return retval;
}

static int track_frame_analog_mode(boxing_tracker_gpf * tracker, const boxing_image8 * input_image)
{
    DLOG_INFO("Retrieving marks");

    BASEMEMBER(base.x_sampling_rate) = (boxing_float) (input_image->width/SMEMBER(frame_dimension).x);
    BASEMEMBER(base.y_sampling_rate) = (boxing_float) (input_image->height/SMEMBER(frame_dimension).y);

    tracker->metadata_sampler->state = DTRUE;
    tracker->content_sampler->state = DTRUE;

    boxing_corner_mark_definition definition;
    definition.corner_mark_symbol = (boxing_float)SMEMBER(corner_mark_dimension).x;
    definition.corner_mark_gap = (boxing_float)SMEMBER(corner_mark_gap_size);
    definition.printable_area_width = (float)SMEMBER(frame_dimension).x;
    definition.printable_area_height = (float)SMEMBER(frame_dimension).y;
    definition.border = 1;
    definition.border_gap = 1;

    frame_corner_marks corner_marks;
    int res = BOXING_CORNER_MARK_OK;

#ifdef BOXINGLIB_CALLBACK
    res = BASEBASEMEMBER( track_corner_mark )(tracker->base.user_data, input_image, &definition, BASEMEMBER( base.x_sampling_rate ), BASEMEMBER( base.y_sampling_rate ), &corner_marks);

    if ( BASEBASEMEMBER( on_corner_mark_complete ) )
    {
        res = BASEBASEMEMBER(on_corner_mark_complete)(BASEBASEMEMBER(user_data), &res, &corner_marks);
    }
#else
    res = BASEBASEMEMBER( track_corner_mark )(NULL, input_image, &definition, BASEMEMBER( base.x_sampling_rate ), BASEMEMBER( base.y_sampling_rate ), &corner_marks);
#endif


    if (res!=BOXING_CORNER_MARK_OK)
    {
        DLOG_ERROR("track_frame_analog_mode  Finding reference marks failed");
        return 1;
    }


    boxing_pointi dimension = boxing_corner_mark_dimension( &corner_marks );
    BASEMEMBER(base.x_sampling_rate) = (boxing_float)(dimension.x)/(boxing_float)(SMEMBER(frame_dimension).x - SMEMBER(corner_mark_dimension).x);
    BASEMEMBER(base.y_sampling_rate) = (boxing_float)(dimension.y)/(boxing_float)(SMEMBER(frame_dimension).y - SMEMBER(corner_mark_dimension).y);

    DLOG_INFO1("Horizontal sampling rate = %f", BASEMEMBER(base.x_sampling_rate));
    DLOG_INFO1("Vertical sampling rate   = %f", BASEMEMBER(base.y_sampling_rate));

    const boxing_pointi * corners[4] = { &corner_marks.top_left, &corner_marks.top_right, &corner_marks.bottom_left, &corner_marks.bottom_right };
    boxing_frame_tracker_util_calculate_average_maxmin(input_image, corners, 4, BASEMEMBER(base.x_sampling_rate), BASEMEMBER(base.y_sampling_rate), &SMEMBER(avg_max), &SMEMBER(avg_min));
    DLOG_INFO2("Average max, min = %f, %f", &SMEMBER(avg_max), &SMEMBER(avg_min));

    if(!calculate_reference_bars((boxing_tracker_gpf_b1 *)tracker, input_image, &corner_marks, &SMEMBER(reference_bars)))
    {
        return 1;
    }

    const gvector bar_top_points   = SMEMBER(reference_bars).top_reference_bar;
    const gvector bar_left_points  = SMEMBER(reference_bars).left_reference_bar;
    const gvector bar_down_points  = SMEMBER(reference_bars).bottom_reference_bar;
    const gvector bar_right_points = SMEMBER(reference_bars).right_reference_bar;

    boxing_float * horizontal_left_shift  = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY_CLEAR(boxing_float, input_image->height);
    boxing_float * horizontal_right_shift = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY_CLEAR(boxing_float, input_image->height);

    if(tracker->base.mode & BOXING_TRACK_HORIZONTAL_SHIFT)
    {
        boxing_pointi hstl = {(int)( corner_marks.top_left.x-(16-1)*BASEMEMBER(base.x_sampling_rate) ),
                              (int)( corner_marks.top_left.y-(8-1)*BASEMEMBER(base.y_sampling_rate) )};
        boxing_pointi hsbl = {(int)( corner_marks.bottom_left.x-(16-1)*BASEMEMBER(base.x_sampling_rate) ),
                              (int)( corner_marks.bottom_left.y+(8-1)*BASEMEMBER(base.y_sampling_rate) )};
        calc_horizontal_offset((boxing_tracker_gpf_b1 *)tracker, input_image,
                hstl, hsbl, horizontal_left_shift, BOXING_TRACKERUTIL_SCAN_DIRECTION_LEFT_TO_RIGHT);

        boxing_pointi hstr = {(int)( corner_marks.top_right.x+(16-1)*BASEMEMBER(base.x_sampling_rate) ),
                              (int)( corner_marks.top_right.y-(8-1)*BASEMEMBER(base.y_sampling_rate) )};
        boxing_pointi hsbr = {(int)( corner_marks.bottom_right.x+(16-1)*BASEMEMBER(base.x_sampling_rate) ),
                              (int)( corner_marks.bottom_right.y+(8-1)*BASEMEMBER(base.y_sampling_rate) )};
        calc_horizontal_offset((boxing_tracker_gpf_b1 *)tracker, input_image,
                hstr, hsbr, horizontal_right_shift, BOXING_TRACKERUTIL_SCAN_DIRECTION_RIGHT_TO_LEFT);
    }

    DLOG_INFO("Retrieving data locations");
    if( !calculate_sampling_location((boxing_tracker_gpf_b1 *)tracker,
                                   (boxing_pointf*)bar_left_points.buffer,  (const int)bar_left_points.size,
                                   (boxing_pointf*)bar_right_points.buffer, (const int)bar_right_points.size,
                                   (boxing_pointf*)bar_top_points.buffer,   (const int)bar_top_points.size,
                                   (boxing_pointf*)bar_down_points.buffer,  (const int)bar_down_points.size,
                                   horizontal_left_shift,
                                   horizontal_right_shift,
                                   input_image->height,
                                   SMEMBER(frame_dimension).x,
                                   SMEMBER(corner_mark_dimension).x, 
                                   &corner_marks))
    {
        DLOG_ERROR("track_frame_analog_mode Recovery reference points is not possible. STOP process");
        boxing_memory_free(horizontal_right_shift);
        boxing_memory_free(horizontal_left_shift);
        return 1;
    }

    boxing_memory_free(horizontal_right_shift);
    boxing_memory_free(horizontal_left_shift);

    if(tracker->base.mode & BOXING_TRACK_HORIZONTAL_SHIFT)
    {
        boxing_matrixf displacement_matrix;
        boxing_matrixf_init_in_place(&displacement_matrix, 0, 0); // \todo 0x0 matrix?
        get_displacement_matrix((boxing_tracker_gpf_b1 *)tracker, input_image, &displacement_matrix);
        boxing_frame_tracker_util_add_displacement(&displacement_matrix, &tracker->content_sampler->location_matrix);
        boxing_memory_free(displacement_matrix.data);
    }
    return 0;
}

static void get_displacement_matrix(boxing_tracker_gpf_b1 * tracker, const boxing_image8 * image, boxing_matrixf * displacement_matrix)
{
     boxing_matrixf_init_in_place(displacement_matrix, 3, 3);
     boxing_memory_clear(displacement_matrix->data, 3 * 3 * sizeof(boxing_pointf));

     boxing_pointi start;
     boxing_pointi stop;
     boxing_pointf reference_point;
     boxing_float yoffset;
     boxing_pointf left_edge_location;
     boxing_pointf center_edge_location;
     boxing_pointf right_edge_location;

     int line_points_size = tracker->base.content_sampler->location_matrix.width;

     // top
     boxing_pointf * line_points =  tracker->base.content_sampler->location_matrix.data;
     reference_point = (line_points)[25];
     start.x = (int)( reference_point.x );
     start.y = (int)( reference_point.y-4*BASEMEMBER(base.y_sampling_rate) );
     stop.x =  (int)( reference_point.x );
     stop.y =  (int)( reference_point.y );
     left_edge_location = boxing_frame_tracker_util_find_h_reference_bar_edge(image, start, stop, BASEMEMBER(base.x_sampling_rate), BASEMEMBER(base.y_sampling_rate));
     left_edge_location.x += start.x;
     left_edge_location.y += start.y;

     reference_point = (line_points)[line_points_size/2 - 1];
     start.x = (int)( reference_point.x );
     start.y = (int)( reference_point.y-4*BASEMEMBER(base.y_sampling_rate) );
     stop.x =  (int)( reference_point.x );
     stop.y =  (int)( reference_point.y );
     center_edge_location = boxing_frame_tracker_util_find_h_reference_bar_edge(image, start, stop, BASEMEMBER(base.x_sampling_rate), BASEMEMBER(base.y_sampling_rate));
     center_edge_location.x += start.x;
     center_edge_location.y += start.y;

     reference_point = (line_points)[line_points_size - 25];
     start.x = (int)( reference_point.x );
     start.y = (int)( reference_point.y-4*BASEMEMBER(base.y_sampling_rate) );
     stop.x =  (int)( reference_point.x );
     stop.y =  (int)( reference_point.y );
     right_edge_location = boxing_frame_tracker_util_find_h_reference_bar_edge(image, start, stop, BASEMEMBER(base.x_sampling_rate), BASEMEMBER(base.y_sampling_rate));
     right_edge_location.x += start.x;
     right_edge_location.y += start.y;

     yoffset = center_edge_location.y - (left_edge_location.y+right_edge_location.y)/2;
     MATRIX_ELEMENT(displacement_matrix, 0, 1).x = 0;
     MATRIX_ELEMENT(displacement_matrix, 0, 1).y = (boxing_float)yoffset;

     // bottom
     line_points =  MATRIX_ROW(&tracker->base.content_sampler->location_matrix, tracker->base.content_sampler->location_matrix.height-1);
     reference_point = (line_points)[25];
     start.x = (int)( reference_point.x );
     start.y = (int)( reference_point.y+4*BASEMEMBER(base.y_sampling_rate) );
     stop.x =  (int)( reference_point.x );
     stop.y =  (int)( reference_point.y );
     left_edge_location = boxing_frame_tracker_util_find_h_reference_bar_edge(image, start, stop, BASEMEMBER(base.x_sampling_rate), BASEMEMBER(base.y_sampling_rate));
     left_edge_location.x += start.x;
     left_edge_location.y += start.y;

     reference_point = (line_points)[line_points_size/2 - 1];
     start.x = (int)( reference_point.x );
     start.y = (int)( reference_point.y+4*BASEMEMBER(base.y_sampling_rate) );
     stop.x =  (int)( reference_point.x );
     stop.y =  (int)( reference_point.y );
     center_edge_location = boxing_frame_tracker_util_find_h_reference_bar_edge(image, start, stop, BASEMEMBER(base.x_sampling_rate), BASEMEMBER(base.y_sampling_rate));
     center_edge_location.x += start.x;
     center_edge_location.y += start.y;

     reference_point = (line_points)[line_points_size - 25];
     start.x = (int)( reference_point.x );
     start.y = (int)( reference_point.y+4*BASEMEMBER(base.y_sampling_rate) );
     stop.x =  (int)( reference_point.x );
     stop.y =  (int)( reference_point.y );
     right_edge_location = boxing_frame_tracker_util_find_h_reference_bar_edge(image, start, stop, BASEMEMBER(base.x_sampling_rate), BASEMEMBER(base.y_sampling_rate));
     right_edge_location.x += start.x;
     right_edge_location.y += start.y;

     yoffset = center_edge_location.y - (left_edge_location.y+right_edge_location.y)/2;
     MATRIX_ELEMENT(displacement_matrix, 2, 1).x = 0;
     MATRIX_ELEMENT(displacement_matrix, 2, 1).y = (boxing_float)yoffset;
     MATRIX_ELEMENT(displacement_matrix, 1, 1).x = (MATRIX_ELEMENT(displacement_matrix, 2, 1).x + MATRIX_ELEMENT(displacement_matrix, 0, 1).x)/2;
     MATRIX_ELEMENT(displacement_matrix, 1, 1).y = (MATRIX_ELEMENT(displacement_matrix, 2, 1).y + MATRIX_ELEMENT(displacement_matrix, 0, 1).y)/2;

     DLOG_INFO("Dislacement matrix");
     DLOG_INFO3("(%f %f %f)", MATRIX_ELEMENT(displacement_matrix, 0, 0).y, MATRIX_ELEMENT(displacement_matrix, 0, 1).y, MATRIX_ELEMENT(displacement_matrix, 0, 2).y);
     DLOG_INFO3("(%f %f %f)", MATRIX_ELEMENT(displacement_matrix, 1, 0).y, MATRIX_ELEMENT(displacement_matrix, 1, 1).y, MATRIX_ELEMENT(displacement_matrix, 1, 2).y);
     DLOG_INFO3("(%f %f %f)", MATRIX_ELEMENT(displacement_matrix, 2, 0).y, MATRIX_ELEMENT(displacement_matrix, 2, 1).y, MATRIX_ELEMENT(displacement_matrix, 2, 2).y);

}

static DBOOL calc_horizontal_offset(boxing_tracker_gpf_b1 * tracker,
                                 const boxing_image8 * image,
                                 const boxing_pointi from,
                                 const boxing_pointi to,
                                 boxing_float * offsets,
                                 int scan_direction)
{
    boxing_float * border_location = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_float, image->height);
    boxing_frame_tracker_util_track_vertical_border(image, &from, &to, border_location, scan_direction, BASEMEMBER(base.x_sampling_rate));

    gvector * top = &tracker->reference_bars.top_reference_bar;
    gvector * bottom = &tracker->reference_bars.bottom_reference_bar;

    boxing_pointf start = GVECTORN(top, boxing_pointf, 0);
    boxing_pointf end = GVECTORN(bottom, boxing_pointf, 0);

    if(scan_direction == BOXING_TRACKERUTIL_SCAN_DIRECTION_RIGHT_TO_LEFT)
    {
        start = GVECTORN(top, boxing_pointf, top->size-1);
        end = GVECTORN(bottom, boxing_pointf, bottom->size-1);
    }

    boxing_float dx = (boxing_float)(border_location[(int)end.y] - border_location[(int)start.y]) / (boxing_float)(end.y - start.y);
    for(int y = from.y; y < to.y; y++)
    {
        boxing_float x = dx*(y - start.y) + border_location[(int)start.y];
        offsets[y] = (boxing_float)-(x - border_location[y]);

        assert( isfinite( offsets[y] ) );
    }

    return DTRUE;

}

static DBOOL calculate_reference_bars(boxing_tracker_gpf_b1 * tracker,
                                   const boxing_image8 * image,
                                   frame_corner_marks * corner_marks,
                                   frame_reference_bars * reference_bars)
{

    boxing_pointi * top_left_corner_mark =     &corner_marks->top_left;
    boxing_pointi * top_right_corner_mark =    &corner_marks->top_right;
    boxing_pointi * bottom_left_corner_mark =  &corner_marks->bottom_left;
    boxing_pointi * bottom_right_corner_mark = &corner_marks->bottom_right;

    boxing_pointf start_left;
    start_left.x = top_left_corner_mark->x + 15.5f * BASEMEMBER(base.x_sampling_rate);
    start_left.y = top_left_corner_mark->y + 8 * BASEMEMBER(base.y_sampling_rate);
    boxing_pointf end_right;
    end_right.x = top_right_corner_mark->x - 16 * BASEMEMBER(base.x_sampling_rate);
    end_right.y = top_right_corner_mark->y + 8 * BASEMEMBER(base.y_sampling_rate);
    gvector_resize(&reference_bars->top_reference_bar, tracker->contaner_dimension.x);
    if (!boxing_frame_tracker_util_track_reference_bar(image,
                                             &start_left,
                                             &end_right,
                                             (boxing_pointf*)reference_bars->top_reference_bar.buffer,
                                             (int)reference_bars->top_reference_bar.size,
                                             &start_left,
                                             1))
    {
        DLOG_ERROR("calculate_reference_bars Failed tracking top reference bar");
        return DFALSE;
    }    

    start_left.x = bottom_left_corner_mark->x + 15.5f * BASEMEMBER(base.x_sampling_rate);
    start_left.y = bottom_left_corner_mark->y - 8 * BASEMEMBER(base.y_sampling_rate);
    
    end_right.x = bottom_right_corner_mark->x - 16 * BASEMEMBER(base.x_sampling_rate);
    end_right.y = bottom_right_corner_mark->y - 8 * BASEMEMBER(base.y_sampling_rate);
    gvector_resize(&reference_bars->bottom_reference_bar, tracker->contaner_dimension.x);
    if (!boxing_frame_tracker_util_track_reference_bar(image,
                                                       &start_left,
                                                       &end_right,
                                                       (boxing_pointf*)reference_bars->bottom_reference_bar.buffer,
                                                       (int)reference_bars->bottom_reference_bar.size,
                                                       &start_left,
                                                       1))
    {
        DLOG_ERROR("calculate_reference_bars Failed tracking bottom reference bar");
        return DFALSE;
    }    
        
    boxing_pointf start_top;
    start_top.x = top_left_corner_mark->x + 8 * BASEMEMBER(base.x_sampling_rate);
    start_top.y = top_left_corner_mark->y + 15.5f * BASEMEMBER(base.y_sampling_rate);
    boxing_pointf end_bottom;
    end_bottom.x = bottom_left_corner_mark->x + 8 * BASEMEMBER(base.x_sampling_rate);
    end_bottom.y = bottom_left_corner_mark->y - 16 * BASEMEMBER(base.y_sampling_rate);
    gvector_resize(&reference_bars->left_reference_bar, tracker->contaner_dimension.y);
    if (!boxing_frame_tracker_util_track_reference_bar(image,
                                                       &start_top,
                                                       &end_bottom,
                                                       (boxing_pointf*)reference_bars->left_reference_bar.buffer,
                                                       (int)reference_bars->left_reference_bar.size,
                                                       &start_top,
                                                       1))
    {
        DLOG_ERROR("calculate_reference_bars Failed tracking left reference bar");
        return DFALSE;
    }
        
    start_top.x = top_right_corner_mark->x - 8 * BASEMEMBER(base.x_sampling_rate);
    start_top.y = top_right_corner_mark->y + 15.5f * BASEMEMBER(base.y_sampling_rate);
    
    end_bottom.x = bottom_right_corner_mark->x - 8 * BASEMEMBER(base.x_sampling_rate);
    end_bottom.y = bottom_right_corner_mark->y - 16 * BASEMEMBER(base.y_sampling_rate);
    gvector_resize(&reference_bars->right_reference_bar, tracker->contaner_dimension.y);
    if (!boxing_frame_tracker_util_track_reference_bar(image,
                                                       &start_top,
                                                       &end_bottom,
                                                       (boxing_pointf*)reference_bars->right_reference_bar.buffer,
                                                       (int)reference_bars->right_reference_bar.size,
                                                       &start_top,
                                                       1))
    {
        DLOG_ERROR("calculate_reference_bars Failed tracking right reference bar");
        return DFALSE;
    }

    return DTRUE;
}

#ifdef DEBUG
static inline DBOOL intersect_debug(const boxing_linef la, const boxing_linef lb, boxing_pointf *intersection_point) 
{
    // ipmlementation is based on Graphics Gems III's "Faster Line Segment Intersection"
    const boxing_pointf a = {la.p2.x - la.p1.x, la.p2.y - la.p1.y};
    const boxing_pointf b = {lb.p1.x - lb.p2.x, lb.p1.y - lb.p2.y};
    const boxing_pointf c = {la.p1.x - lb.p1.x, la.p1.y - lb.p1.y};

    const boxing_float denominator = a.y * b.x - a.x * b.y;
    if (denominator == 0 || !isfinite( denominator ) )
        return DFALSE;

    const boxing_float reciprocal = 1 / denominator;
    const boxing_float na = (b.y * c.x - b.x * c.y) * reciprocal;
    intersection_point->x = (boxing_float)( la.p1.x + a.x * na );
    intersection_point->y = (boxing_float)( la.p1.y + a.y * na );

    return DTRUE;
}
#endif

static inline void intersect_fast(const boxing_linef la, const boxing_pointf lb1, const boxing_pointf lb_dir, boxing_pointf *intersection_point) 
{
    // ipmlementation is based on Graphics Gems III's "Faster Line Segment Intersection"
    const boxing_pointf a = {la.p2.x - la.p1.x, la.p2.y - la.p1.y};
    const boxing_pointf b = lb_dir;
    const boxing_pointf c = {la.p1.x - lb1.x, la.p1.y - lb1.y};

    const boxing_float denominator = a.y * b.x - a.x * b.y;

    const boxing_float na = (b.y * c.x - b.x * c.y) / denominator;
    intersection_point->x = (boxing_float)( la.p1.x + a.x * na );
    intersection_point->y = (boxing_float)( la.p1.y + a.y * na );
}

static boxing_linef * calculate_lines(
    const boxing_pointf * start_points, const int start_points_size,
    const boxing_pointf * end_points, const int end_points_size,
    boxing_linef * lines)
{
    int line_count = start_points_size > end_points_size ? end_points_size : start_points_size;
    boxing_linef * line = lines;
    const boxing_pointf * line_start = start_points;
    const boxing_pointf * line_end = end_points;

    while(line_count)
    {
        line->p1 = *line_start;
        line->p2 = *line_end;
        line++;
        line_start++;
        line_end++;
        line_count--;
    }

    return lines;
}

static DBOOL calculate_sampling_location(
    boxing_tracker_gpf_b1 * tracker,
    const boxing_pointf * left_bar_points,      const int left_bar_points_size,
    const boxing_pointf * right_bar_points,     const int right_bar_points_size,
    const boxing_pointf * top_bar_points,       const int top_bar_points_size,
    const boxing_pointf * bottom_bar_points,    const int bottom_bar_points_size,
    const boxing_float * left_offset_xs,
    const boxing_float * right_offset_xs,
    int     offsets_size,
    int     width,
    int     vertical_barwidth,
    const frame_corner_marks* corner_marks )
{

    if(left_bar_points_size != right_bar_points_size)
    {
        DLOG_ERROR("calculate_sampling_location Left bar points size != right bar points size");
        return DFALSE;
    }

    if(left_bar_points_size == 0)
    {
        DLOG_ERROR("calculate_sampling_location No vertical points");
        return DFALSE;
    }

    if(top_bar_points_size != bottom_bar_points_size)
    {
        DLOG_ERROR("calculate_sampling_location Top bar points size != bottom bar points size");
        return DFALSE;
    }

    if(top_bar_points_size == 0)
    {
        DLOG_ERROR("calculate_sampling_location No horizontal points");
        return DFALSE;
    }

    // Group data for better cache performance
    boxing_float* offsets = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_float, offsets_size * 2);
    boxing_float* p = offsets;
    for ( int i = 0; i < offsets_size; i++ )
    {
        *p = left_offset_xs[i];
        p++;
        *p = right_offset_xs[i];
        p++;
    }

    // calculate horizontal lines
    int line_count_h = left_bar_points_size > right_bar_points_size ? right_bar_points_size : left_bar_points_size;
    boxing_linef * horizontal_lines = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_linef, line_count_h);
    calculate_lines(&left_bar_points[0], left_bar_points_size, &right_bar_points[0], (int)right_bar_points_size, horizontal_lines);


    // calculate vertical lines
    int line_count_v = top_bar_points_size > bottom_bar_points_size ? bottom_bar_points_size : top_bar_points_size;
    boxing_linef * vertical_lines = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_linef, line_count_v);
    calculate_lines(top_bar_points, top_bar_points_size, bottom_bar_points, bottom_bar_points_size, vertical_lines);

    DBOOL retval = DTRUE;
    retval &= calculate_intersection_points(horizontal_lines,
                                          vertical_lines,
                                          line_count_h,
                                          line_count_v,
                                          offsets,
                                          (boxing_float)vertical_barwidth,
                                          (boxing_float)width,
                                          &tracker->base.content_sampler->location_matrix);
    boxing_memory_free(horizontal_lines);

    // calculate metadata_vertical_lines lines
    int metadata_count_v = line_count_v/4;
    boxing_linef * metadata_vertical_lines = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_linef, metadata_count_v);
    for(int i = 0; i < metadata_count_v; i++)
    {
        metadata_vertical_lines[i] = vertical_lines[i*4+2];
    }
    boxing_memory_free(vertical_lines);

    // calculate metadata_horizontal_lines lines
    const int metadata_count_h = 4;
    boxing_pointf metadata_right_bar_points[4];
    boxing_pointf metadata_left_bar_points[4];
    for(int i = 0; i < 4; i++)
    {
        metadata_left_bar_points[i].x =  (boxing_float)corner_marks->bottom_left.x;
        metadata_left_bar_points[i].y =  corner_marks->bottom_left.y+BASEMEMBER(base.y_sampling_rate)*(2+4*i);
        metadata_right_bar_points[i].x = (boxing_float)corner_marks->bottom_right.x;
        metadata_right_bar_points[i].y = corner_marks->bottom_right.y+BASEMEMBER(base.y_sampling_rate)*(2+4*i);
    }


    
    boxing_linef * metadata_horizontal_lines = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_linef, metadata_count_h);
    calculate_lines(&metadata_left_bar_points[0], 4, &metadata_right_bar_points[0], 4, metadata_horizontal_lines);


    retval &= calculate_intersection_points(metadata_horizontal_lines,
                                          metadata_vertical_lines,
                                          metadata_count_h,
                                          metadata_count_v,
                                          offsets,
                                          (boxing_float)vertical_barwidth,
                                          (boxing_float)width,
                                          &tracker->base.metadata_sampler->location_matrix);
    boxing_memory_free(metadata_horizontal_lines);
    boxing_memory_free(metadata_vertical_lines);
    boxing_memory_free(offsets);

    return retval;
}

static DBOOL calculate_intersection_points(
    const boxing_linef * horizontal_lines, 
    const boxing_linef * vertical_lines,
    int lines_size_h,
    int lines_size_v,
    const boxing_float * offsets,
    const boxing_float vertical_barwidth,
    const boxing_float width,
    boxing_matrixf * intersections_points)
{
    const boxing_linef * horizontal_line = horizontal_lines;
    boxing_pointf * points = intersections_points->data;
    const boxing_linef * vertical_line = vertical_lines;

    boxing_float index_w=0;
    for (int i = 0; i < lines_size_h; i += 1)
    {
        vertical_line = vertical_lines;
        boxing_pointf * point = points;
        index_w = 0.0f;
        boxing_pointf la = horizontal_line->p1;
        boxing_pointf lb = horizontal_line->p2;
        boxing_pointf ldir = boxing_math_pointf_subtract( &la, &lb );
        for (int j = 0; j < lines_size_v; j += 1)
        {
            boxing_pointf intersection;

#ifdef DEBUG
            if ( !intersect_debug( *vertical_line, *horizontal_line, &intersection) )
            {
                return DFALSE;
            }
#else
            intersect_fast( *vertical_line, la, ldir, &intersection );
#endif
            boxing_float k = (index_w + vertical_barwidth)/width;
            int   offset_index = (int)(intersection.y+0.5);
            
            const boxing_float *left_offset = offsets + offset_index * 2;
            const boxing_float *right_offset = left_offset + 1;
            boxing_float horizontal_shift = *left_offset * (1-k) + *right_offset * k;

            assert( isfinite( intersection.x + horizontal_shift ) );
            assert( isfinite( intersection.y ) );

            point->x = intersection.x + horizontal_shift;
            point->y = intersection.y;
            ++point;
            ++vertical_line;
            ++index_w;
        }
        points += intersections_points->width;
        horizontal_line++;
    }
    return DTRUE;
}
