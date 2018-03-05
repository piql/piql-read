/*****************************************************************************
**
**  Implementation of the genericframegpf_1 interface
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
#include <boxing/utils.h>
#include    "boxing/graphics/genericframegpf_b1.h"
#include    "boxing/graphics/referencepoint.h"
#include    "boxing/graphics/referencebar.h"
#include    "boxing/graphics/calibrationbar.h"
#include    "boxing/graphics/contentcontainer.h"
#include    "boxing/graphics/metadatabar.h"
#include    "boxing/graphics/label.h"
#include    "boxing/graphics/border.h"
#include    "frametrackergpf_b1.h"

//  BASE INCLUDES
//
#include    "boxing/log.h"
#include    "boxing/platform/memory.h"
#include "../frame/frametrackergpf_b1.h"

//  DEFINES
//

#define SMEMBER(member) (((boxing_frame_gpf_b1*)frame)->member)

//  PRIVATE INTERFACE
//

static void          add_components(boxing_frame_gpf_b1 * frame);
static int           frame_max_levels_per_symbol(boxing_frame * frame);
static int           frame_levels_per_symbol(boxing_frame * frame);
static void          set_levels_per_symbol(boxing_frame * frame, int levels, DBOOL force /*  = false */);
static void          set_system_human_readable(boxing_frame * frame, const char * str);
static void          set_user_human_readable(boxing_frame * frame, const char * str);
static void          render(boxing_frame * frame, boxing_painter * painter);
static boxing_pointi size(const boxing_frame * frame);

static int           impl_capasity(boxing_generic_container * component);
static boxing_pointi impl_dimension(boxing_generic_container * component);
static boxing_pointi impl_location(boxing_generic_container * component);
static void          impl_set_data(boxing_generic_container * component, const char * data, int size);

static int           imeta_capasity(boxing_generic_container * component);
static boxing_pointi imeta_dimension(boxing_generic_container * component);
static boxing_pointi imeta_location(boxing_generic_container * component);
static void          imeta_set_data(boxing_generic_container * component, const char * data, int size);
static boxing_pointi imeta_tile_size(boxing_generic_container * component);

static boxing_generic_container *  frame_gpf_b1_container(boxing_frame * frame);
static boxing_generic_container *  frame_gpf_b1_metadata_container(boxing_frame * frame);
static struct boxing_tracker_s *   frame_gpf_b1_create_frame_tracker(boxing_frame * frame);

static void boxing_frame_gpf_b1_free(boxing_frame * frame);


// PUBLIC GENERIC FRAME GPF B1 FUNCTIONS
//

void boxing_frame_gpf_b1_init(boxing_frame_gpf_b1 * frame, int width, int height, int border/* = 1*/, int border_gap/* = 1*/,
                    int corner_mark_size/* = 32*/, int corner_mark_gap/* = 1*/, int tiles_per_column/* = 4*/, int max_levels_per_symbol/* = 2*/)
{
    BOXING_UNUSED_PARAMETER( border_gap );
    BOXING_UNUSED_PARAMETER( border );

    boxing_generic_frame_init(&frame->base);
    frame->max_levels_per_symbol = max_levels_per_symbol;
    frame->levels_per_symbol = max_levels_per_symbol;
    frame->corner_mark_size = corner_mark_size;
    frame->corner_mark_gap = corner_mark_gap;
    frame->tiles_per_column = tiles_per_column;

    frame->base.free = boxing_frame_gpf_b1_free;
    frame->base.max_levels_per_symbol = frame_max_levels_per_symbol;
    frame->base.levels_per_symbol = frame_levels_per_symbol;
    frame->base.set_levels_per_symbol = set_levels_per_symbol;
    frame->base.set_system_human_readable = set_system_human_readable;
    frame->base.set_user_human_readable = set_user_human_readable;
    frame->base.render = render;
    frame->base.size = size;
    frame->base.container = frame_gpf_b1_container;
    frame->base.metadata_container = frame_gpf_b1_metadata_container;
    frame->base.create_frame_tracker = frame_gpf_b1_create_frame_tracker;


    boxing_container_impl_b1_init(&frame->generic_container, frame);
    boxing_container_imeta_b1_init(&frame->generic_metadata_container, frame);

    boxing_component_init(&frame->base_component, NULL);


    // populate component
    add_components(frame);

    // update all subcomponents with new dimensions
    boxing_frame_gpf_b1_set_size(frame, width, height);
    
    frame->base_component.foreground_color = max_levels_per_symbol - 1;
    frame->base_component.background_color = 0;
    frame->base_component.pos.x = 0;
    frame->base_component.pos.y = 0;
}


void boxing_frame_gpf_b1_add(boxing_frame_gpf_b1 * frame, boxing_component * child)
{
    if( !child )
        return;

    child->parent = &frame->base_component;
    frame->base_component.add(&frame->base_component, child);
}


void boxing_frame_gpf_b1_set_size(boxing_frame_gpf_b1 * frame, int width, int height)
{
    frame->base_component.set_size(&frame->base_component, width, height);

    const int frame_width                      = width;
    const int frame_height                     = height;
    const int calibration_band_width           = frame_width - 2 * frame->corner_mark_size;
    const int calibration_band_height          = frame->corner_mark_size / 2;
    const int metadata_band_width              = frame_width - 2 * frame->corner_mark_size;
    const int metadata_band_height             = frame->corner_mark_size / 2;
    const int user_content_width               = frame->corner_mark_size / 2;
    const int user_content_height              = frame_height - 2 * frame->corner_mark_size;
    const int content_width                    = frame_width  - 2 * frame->corner_mark_size;
    const int content_height                   = frame_height - 2 * frame->corner_mark_size;
    const int reference_band_width             = frame->corner_mark_size / 2;
    const int horizontal_reference_band_length = frame_width  - 2 * frame->corner_mark_size;
    const int vertical_reference_band_length   = frame_height - 2 * frame->corner_mark_size;


    // top left corner mark;
    frame->top_left_reference_point->base.set_size((boxing_component *)frame->top_left_reference_point, frame->corner_mark_size-1, frame->corner_mark_size-1);
    frame->top_left_reference_point->base.pos.x = 1;
    frame->top_left_reference_point->base.pos.y = 1;

    // top right corner mark;
    frame->top_right_reference_point->base.set_size((boxing_component *)frame->top_right_reference_point, frame->corner_mark_size-1, frame->corner_mark_size-1);
    frame->top_right_reference_point->base.pos.x = frame_width - frame->corner_mark_size+1;
    frame->top_right_reference_point->base.pos.y = 0+1;

    // bottom left corner mark;
    frame->bottom_left_reference_point->base.set_size((boxing_component *)frame->bottom_left_reference_point, frame->corner_mark_size-1, frame->corner_mark_size-1);
    frame->bottom_left_reference_point->base.pos.x = 0+1;
    frame->top_right_reference_point->base.pos.y = frame_height - frame->corner_mark_size+1;

    // bottom right corner mark;
    frame->bottom_right_reference_point->base.set_size((boxing_component *)frame->bottom_right_reference_point, frame->corner_mark_size-1, frame->corner_mark_size-1);
    frame->bottom_right_reference_point->base.pos.x = frame_width - frame->corner_mark_size+1;
    frame->top_right_reference_point->base.pos.y = frame_height - frame->corner_mark_size+1;

    // top reference bar
    frame->top_reference_bar->base.set_size((boxing_component *)frame->top_reference_bar, horizontal_reference_band_length, reference_band_width);
    frame->top_reference_bar->base.pos.x = frame->corner_mark_size;
    frame->top_right_reference_point->base.pos.y = frame->corner_mark_size/2;

    // bottom reference bar
    frame->bottom_reference_bar->base.set_size((boxing_component *)frame->bottom_reference_bar, horizontal_reference_band_length, reference_band_width);
    frame->bottom_reference_bar->base.pos.x = frame->corner_mark_size;
    frame->top_right_reference_point->base.pos.y = frame_height - frame->corner_mark_size;

    // left reference bar
    frame->left_reference_bar->base.set_size((boxing_component *)frame->left_reference_bar, reference_band_width, vertical_reference_band_length);
    frame->left_reference_bar->base.pos.x = frame->corner_mark_size/2;
    frame->top_right_reference_point->base.pos.y = frame->corner_mark_size;

    // right reference bar
    frame->right_reference_bar->base.set_size((boxing_component *)frame->right_reference_bar, reference_band_width, vertical_reference_band_length);
    frame->right_reference_bar->base.pos.x = frame_width - frame->corner_mark_size;
    frame->top_right_reference_point->base.pos.y = frame->corner_mark_size;

    // calabration bar
    frame->calibration_bar->base.set_size((boxing_component *)frame->calibration_bar, calibration_band_width, calibration_band_height);
    frame->calibration_bar->base.pos.x = frame->corner_mark_size;
    frame->top_right_reference_point->base.pos.y = 0;

    // content container
    frame->content_container->base.set_size((boxing_component *)frame->content_container, content_width, content_height);
    frame->content_container->base.pos.x = frame->corner_mark_size;
    frame->top_right_reference_point->base.pos.y = frame->corner_mark_size;

    // metadata bar
    frame->metadata_bar->base.set_size((boxing_component *)frame->metadata_bar, metadata_band_width, metadata_band_height);
    frame->metadata_bar->base.pos.x = frame->corner_mark_size;
    frame->top_right_reference_point->base.pos.y = frame_height - frame->corner_mark_size/2;

    // system define human readable
    frame->label_system_define->base.set_size((boxing_component *)frame->label_system_define, user_content_width-1, user_content_height);
    frame->label_system_define->base.pos.x = 0;
    frame->top_right_reference_point->base.pos.y = frame->corner_mark_size;

    // user define human readable
    frame->label_user_define->base.set_size((boxing_component *)frame->label_user_define, user_content_width-1, user_content_height);
    frame->label_user_define->base.pos.x = frame_width - user_content_width -1;
    frame->top_right_reference_point->base.pos.y = frame->corner_mark_size;
    
    // user define human readable
    frame->border->base.set_size((boxing_component *)frame->border, frame_width, frame_height);
    frame->border->base.pos.x = frame->border->base.pos.y = 0;

}


boxing_pointi boxing_frame_gpf_b1_corner_mark_dimension(boxing_frame_gpf_b1 * frame)
{
    return frame->top_left_reference_point->base.size;
}


int boxing_frame_gpf_b1_corner_mark_gap_size(boxing_frame_gpf_b1 * frame)
{
    return frame->top_left_reference_point->gap_size;
}


void boxing_container_impl_b1_init(boxing_container_impl_b1 *container, boxing_frame_gpf_b1 * frame)
{
    boxing_generic_frame_container_init(&container->base);
    container->frame = frame;
    container->base.set_data = impl_set_data;
    container->base.location = impl_location;
    container->base.dimension = impl_dimension;
    container->base.capasity = impl_capasity;
}


void boxing_container_imeta_b1_init(boxing_container_imeta_b1 * container, boxing_frame_gpf_b1 *frame)
{
    boxing_generic_frame_container_init(&container->base);
    container->frame = frame;
    container->base.get_tile_size = imeta_tile_size;
    container->base.set_data = imeta_set_data;
    container->base.location = imeta_location;
    container->base.dimension = imeta_dimension;
    container->base.capasity = imeta_capasity;
}


// PRIVATE GENERIC FRAME GPF B1 FUNCTIONS
//

static int imeta_capasity(boxing_generic_container * component)
{
    return ((boxing_container_imeta_b1*)component)->frame->metadata_bar->data_capasity;
}

static boxing_pointi imeta_dimension(boxing_generic_container * component)
{
    return ((boxing_container_imeta_b1*)component)->frame->metadata_bar->base.size;
}

static boxing_pointi imeta_location(boxing_generic_container * component)
{
    return boxing_component_absolute_location((boxing_component *)((boxing_container_imeta_b1*)component)->frame->metadata_bar);
}

static void imeta_set_data(boxing_generic_container * component, const char * data, int size)
{
    boxing_metadata_bar_set_data(((boxing_container_imeta_b1*)component)->frame->metadata_bar, data, size);
}

static boxing_pointi imeta_tile_size(boxing_generic_container * component)
{
    int tile_size = ((boxing_container_imeta_b1*)component)->frame->metadata_bar->tile_size;
    boxing_pointi return_value = { tile_size, tile_size };
    return return_value;
}

static int impl_capasity(boxing_generic_container * component)
{
    return ((boxing_container_impl_b1*)component)->frame->content_container->data_capasity;
}

static boxing_pointi impl_dimension(boxing_generic_container * component)
{
    return ((boxing_container_impl_b1*)component)->frame->content_container->base.size;
}

static boxing_pointi impl_location(boxing_generic_container * component)
{
    return boxing_component_absolute_location((boxing_component *)((boxing_container_impl_b1*)component)->frame->content_container);
}

static void impl_set_data(boxing_generic_container * component, const char * data, int size)
{
    boxing_content_container_set_data(((boxing_container_impl_b1*)component)->frame->content_container, data, size);
}

static void boxing_frame_gpf_b1_free(boxing_frame * frame)
{
    SMEMBER(top_left_reference_point)->base.free((boxing_component*)SMEMBER(top_left_reference_point));
    SMEMBER(top_right_reference_point)->base.free((boxing_component*)SMEMBER(top_right_reference_point));
    SMEMBER(bottom_left_reference_point)->base.free((boxing_component*)SMEMBER(bottom_left_reference_point));
    SMEMBER(bottom_right_reference_point)->base.free((boxing_component*)SMEMBER(bottom_right_reference_point));
    SMEMBER(top_reference_bar)->base.free((boxing_component*)SMEMBER(top_reference_bar));
    SMEMBER(bottom_reference_bar)->base.free((boxing_component*)SMEMBER(bottom_reference_bar));
    SMEMBER(left_reference_bar)->base.free((boxing_component*)SMEMBER(left_reference_bar));
    SMEMBER(right_reference_bar)->base.free((boxing_component*)SMEMBER(right_reference_bar));
    SMEMBER(calibration_bar)->base.free((boxing_component*)SMEMBER(calibration_bar));
    SMEMBER(content_container)->base.free((boxing_component*)SMEMBER(content_container));
    SMEMBER(metadata_bar)->base.free((boxing_component*)SMEMBER(metadata_bar));
    SMEMBER(label_system_define)->base.free((boxing_component*)SMEMBER(label_system_define));
    SMEMBER(label_user_define)->base.free((boxing_component*)SMEMBER(label_user_define));
    SMEMBER(border)->base.free((boxing_component*)SMEMBER(border));

    boxing_memory_free(SMEMBER(top_left_reference_point));
    boxing_memory_free(SMEMBER(top_right_reference_point));
    boxing_memory_free(SMEMBER(bottom_left_reference_point));
    boxing_memory_free(SMEMBER(bottom_right_reference_point));
    boxing_memory_free(SMEMBER(top_reference_bar));
    boxing_memory_free(SMEMBER(bottom_reference_bar));
    boxing_memory_free(SMEMBER(left_reference_bar));
    boxing_memory_free(SMEMBER(right_reference_bar));
    boxing_memory_free(SMEMBER(calibration_bar));
    boxing_memory_free(SMEMBER(content_container));
    boxing_memory_free(SMEMBER(metadata_bar));
    boxing_memory_free(SMEMBER(label_system_define));
    boxing_memory_free(SMEMBER(label_user_define));
    boxing_memory_free(SMEMBER(border));

    boxing_component_free(&SMEMBER(base_component));

    boxing_generic_frame_free(frame);
}

static int frame_max_levels_per_symbol(boxing_frame * frame)
{
    return SMEMBER(max_levels_per_symbol);
}

static int frame_levels_per_symbol(boxing_frame * frame)
{
    return SMEMBER(levels_per_symbol);
}

static void set_levels_per_symbol(boxing_frame * frame, int levels, DBOOL force /*  = false */)
{
    if (levels < 2)
    {
        levels = 2;
    }
    else if ((levels > SMEMBER(max_levels_per_symbol)) && !force)
    {
        levels = SMEMBER(max_levels_per_symbol);
    }
    SMEMBER(levels_per_symbol) = levels;

    SMEMBER(calibration_bar)->levels_per_symbol = SMEMBER(levels_per_symbol);
    SMEMBER(base_component).foreground_color = SMEMBER(levels_per_symbol) - 1;
}

static void set_system_human_readable(boxing_frame * frame, const char * str)
{
    boxing_label_set_label(SMEMBER(label_system_define), str);
}

static void set_user_human_readable(boxing_frame * frame, const char * str)
{
    boxing_label_set_label(SMEMBER(label_user_define), str);
}

static void render(boxing_frame * frame, boxing_painter * painter)
{
    boxing_pointi pos = SMEMBER(base_component).pos;
    boxing_pointi size = frame->size(frame);
    painter->fill_rect(painter, pos.x, pos.y, size.x, size.y, boxing_component_get_background_color(&SMEMBER(base_component)));
    SMEMBER(base_component).render(&SMEMBER(base_component), painter);
}

static boxing_pointi size(const boxing_frame * frame)
{
    return SMEMBER(base_component).size;
}

static void add_components(boxing_frame_gpf_b1 * frame)
{
    // top left corner mark;
    frame->top_left_reference_point = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_point);
    boxing_reference_point_init(frame->top_left_reference_point, frame->corner_mark_gap);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->top_left_reference_point);

    // top right corner mark;
    frame->top_right_reference_point = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_point);
    boxing_reference_point_init(frame->top_right_reference_point, frame->corner_mark_gap);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->top_right_reference_point);

    // bottom left corner mark;
    frame->bottom_left_reference_point = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_point);
    boxing_reference_point_init(frame->bottom_left_reference_point, frame->corner_mark_gap);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->bottom_left_reference_point);

    // bottom right corner mark;
    frame->bottom_right_reference_point = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_point);
    boxing_reference_point_init(frame->bottom_right_reference_point, frame->corner_mark_gap);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->bottom_right_reference_point);

    // top reference bar
    frame->top_reference_bar = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_bar);
    boxing_reference_bar_init(frame->top_reference_bar, BOXING_ALIGN_HORIZONTAL, 1, -1, 0);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->top_reference_bar);

    // bottom reference bar
    frame->bottom_reference_bar = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_bar);
    boxing_reference_bar_init(frame->bottom_reference_bar, BOXING_ALIGN_HORIZONTAL, 1, -1, 0);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->bottom_reference_bar);

    // left reference bar
    frame->left_reference_bar = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_bar);
    boxing_reference_bar_init(frame->left_reference_bar, BOXING_ALIGN_VERTICAL, 1, -1, 0);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->left_reference_bar);

    // right reference bar
    frame->right_reference_bar = BOXING_MEMORY_ALLOCATE_TYPE(boxing_reference_bar);
    boxing_reference_bar_init(frame->right_reference_bar, BOXING_ALIGN_VERTICAL, 1, -1, 0);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->right_reference_bar);

    // calibration bar
    frame->calibration_bar = BOXING_MEMORY_ALLOCATE_TYPE(boxing_calibration_bar);
    boxing_calibration_bar_init(frame->calibration_bar, frame->levels_per_symbol);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->calibration_bar);

    // content container
    frame->content_container = BOXING_MEMORY_ALLOCATE_TYPE(boxing_content_container);
    boxing_content_container_init(frame->content_container);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->content_container);

    // metadata bar
    frame->metadata_bar = BOXING_MEMORY_ALLOCATE_TYPE(boxing_metadata_bar);
    boxing_metadata_bar_init(frame->metadata_bar, frame->tiles_per_column);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->metadata_bar);

    // system define human readable
    frame->label_system_define = BOXING_MEMORY_ALLOCATE_TYPE(boxing_label);
    boxing_label_init(frame->label_system_define, "", BOXING_ALIGN_VERTICAL);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->label_system_define);

    // user define human readable
    frame->label_user_define = BOXING_MEMORY_ALLOCATE_TYPE(boxing_label);
    boxing_label_init(frame->label_user_define, "", BOXING_ALIGN_VERTICAL);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->label_user_define);

    // user define human readable
    frame->border = BOXING_MEMORY_ALLOCATE_TYPE(boxing_border);
    boxing_border_init(frame->border, 1, 0);
    boxing_frame_gpf_b1_add(frame, (boxing_component *)frame->border);
}

static boxing_generic_container *  frame_gpf_b1_container(boxing_frame * frame)
{
    return &SMEMBER(generic_container).base;
}

static boxing_generic_container *  frame_gpf_b1_metadata_container(boxing_frame * frame)
{
    return &SMEMBER(generic_metadata_container).base;
}

static struct boxing_tracker_s * frame_gpf_b1_create_frame_tracker(boxing_frame * frame)
{
    return (struct boxing_tracker_s*)boxing_frame_tracker_gpf_b1_create(((boxing_frame_gpf_b1*)frame));
}
