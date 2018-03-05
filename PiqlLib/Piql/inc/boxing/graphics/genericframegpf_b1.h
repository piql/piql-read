#ifndef DGENERICFRAME_GPF_B1C_H
#define DGENERICFRAME_GPF_B1C_H

/*****************************************************************************
**
**  Definition of the dgenericframegpf_b1 struct
**  Generic Preservation Format Version beta 1.0
** 
**  Creation date:  2014/12/16
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

#ifdef __cplusplus
extern "C" {
#endif

//  PROJECT INCLUDES
//
#include "boxing/graphics/component.h"
#include "boxing/graphics/genericframe.h"


struct boxing_reference_point_s;
struct boxing_reference_bar_s;
struct boxing_calibration_bar_s;
struct boxing_content_container_s;
struct boxing_metadata_bar_s;
struct boxing_label_s;
struct boxing_border_s;

struct boxing_frame_gpf_b1_s;

typedef struct boxing_container_impl_b1_s
{
    boxing_generic_container       base;
    struct boxing_frame_gpf_b1_s * frame;
} boxing_container_impl_b1;


typedef struct boxing_container_imeta_b1_s
{
    boxing_generic_container       base;
    struct boxing_frame_gpf_b1_s * frame;
} boxing_container_imeta_b1;


//============================================================================
//  STRUCT:  boxing_frame_gpf_b1

typedef struct boxing_frame_gpf_b1_s
{
    boxing_frame base;

    boxing_component                   base_component;
    struct boxing_reference_point_s  * top_left_reference_point;
    struct boxing_reference_point_s  * top_right_reference_point;
    struct boxing_reference_point_s  * bottom_left_reference_point;
    struct boxing_reference_point_s  * bottom_right_reference_point;
    struct boxing_reference_bar_s    * top_reference_bar;
    struct boxing_reference_bar_s    * bottom_reference_bar;
    struct boxing_reference_bar_s    * left_reference_bar;
    struct boxing_reference_bar_s    * right_reference_bar;
    struct boxing_calibration_bar_s  * calibration_bar;
    struct boxing_content_container_s* content_container;
    struct boxing_metadata_bar_s     * metadata_bar;
    struct boxing_label_s            * label_system_define;
    struct boxing_label_s            * label_user_define;
    struct boxing_border_s           * border;
                                
    int                           max_levels_per_symbol;
    int                           levels_per_symbol;
    int                           corner_mark_size;
    int                           corner_mark_gap;
    int                           tiles_per_column;
    boxing_container_impl_b1      generic_container;
    boxing_container_imeta_b1     generic_metadata_container;

} boxing_frame_gpf_b1;

void            boxing_container_impl_b1_init(boxing_container_impl_b1 *container, boxing_frame_gpf_b1 * frame);
void            boxing_container_imeta_b1_init(boxing_container_imeta_b1 *container, boxing_frame_gpf_b1 * frame);

void            boxing_frame_gpf_b1_init(boxing_frame_gpf_b1 * frame, int width, int height, int border/* = 1*/, int border_gap/* = 1*/,
                    int corner_mark_size/* = 32*/, int corner_mark_gap/* = 1*/, int tiles_per_column/* = 4*/, int max_levels_per_symbol/* = 2*/);
void            boxing_frame_gpf_b1_add(boxing_frame_gpf_b1 * frame, boxing_component * child);
boxing_pointi   boxing_frame_gpf_b1_corner_mark_dimension(boxing_frame_gpf_b1 * frame);
int             boxing_frame_gpf_b1_corner_mark_gap_size(boxing_frame_gpf_b1 * frame);
void            boxing_frame_gpf_b1_set_size(boxing_frame_gpf_b1 * frame, int width, int height);

//============================================================================

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // DGENERICFRAME_GPF_B1C_H
