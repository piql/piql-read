#ifndef DFRAMETRACKERGPF_1CB_H
#define DFRAMETRACKERGPF_1CB_H

/*****************************************************************************
**
**  Definition of the frame tracker Gpf_1 call back interface
**
**  Creation date:  2017/06/28
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2017 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

#ifdef __cplusplus
extern "C" {
#endif

//  PROJECT INCLUDES
//
#include "boxing/image8.h"
#include "boxing/matrix.h"
#include "boxing/unboxer/cornermark.h"

//---------------------------------------------------------------------------- 
/*!
* @brief The correct_frame_geometry_properties serve as input parameters to the correct_frame_geometry function
* which purpose is to find syncponts and validate their positions.
*/
typedef struct correct_frame_geometry_properties_s
{
    boxing_float sync_point_search_radius;         /**< scan radius in pixels, default = 4.5f */
    DBOOL        sync_point_center_is_bright;      /**< is center pixel white, default = DTRUE */
    boxing_float sync_point_max_allowed_variation; /**< max allowed squared offset variation, default = 0.1f */
} correct_frame_geometry_properties;


//---------------------------------------------------------------------------- 
/*!
*  \brief boxing_correct_frame_geometry_cb uses the sync points at given locations to compensate for
*  any geometrical distortions not detected by the general tracking mechanism.
*
* The input image is a super sampled frame and the symbol_location_matrix contains the
* location of frame's data symbols. Special sync symbols are embedded in the data contaner
* to detect and compensate for geometrical distortions within the super sampled frame.
* Each element in the sync_point_location_index represents sync symbol where the index refers
* an element in the location_matrix in which holds the locatation sync symbol. By searching
* for sync symbols in the proximity of the alleged position given by the index_matrix one can
* determine how much the location matrix is off tracking and compesate for it.
*
* Note: The symbol_location_matrix parameter is modified and finally returned
*
* \param[in] frame                      super sampled frame
* \param[in,out] symbol_location_matrix NxM matrix with coordinates to data symbols within a frame
* \param[in] sync_point_location_index  KxL matrix with indexs to each sync point location found in the symbol_location_matrix
* \param[in] properties                 function properties
*
* \return Return optmized NxM location matrix
*/
typedef boxing_matrixf* (*boxing_correct_frame_geometry_cb)(
    void* user, const boxing_image8 * image, boxing_matrixf *symbol_location_matrix,
    const boxing_matrixi *sync_point_location_index,
    const correct_frame_geometry_properties * properties);

struct boxing_tracker_gpf_1_s; //Forward declaration

typedef DBOOL( *boxing_calculate_sampling_locations_cb )(
    void* user, struct boxing_tracker_gpf_1_s * tracker,
    const boxing_pointf * left_bar_points, const int left_bar_points_size,
    const boxing_pointf * right_bar_points, const int right_bar_points_size,
    const boxing_pointf * top_bar_points, const int top_bar_points_size,
    const boxing_pointf * bottom_bar_points, const int bottom_bar_points_size,
    const boxing_float * left_offset_xs, const boxing_float * right_offset_xs, int offsets_size,
    int width, int vertical_barwidth,
    const frame_corner_marks* corner_marks);

typedef DBOOL( *boxing_track_vertical_shift_cb )(
    void* user, struct boxing_tracker_gpf_1_s* tracker,
    const boxing_image8* input_image);

//============================================================================

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif // DFRAMETRACKERGPF_1_H
