#ifndef DFRAMETRACKERGPF_B1_H
#define DFRAMETRACKERGPF_B1_H

/*****************************************************************************
**
**  Definition of the frame tracker interface 
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
#include "boxing/utils.h"
#include "boxing/frame/frametrackergpfbase.h"
#include "boxing/graphics/genericframegpf_b1.h"

//  SYSTEM INCLUDES
//
#include "gvector.h"


typedef struct frame_reference_bars_s
{
    gvector top_reference_bar; // Vectors of boxing_pointf.
    gvector bottom_reference_bar;
    gvector left_reference_bar;
    gvector right_reference_bar;

} frame_reference_bars;

//============================================================================
//  STRUCT:  boxing_tracker_gpf_b1

typedef struct boxing_tracker_gpf_b1_s
{
    boxing_tracker_gpf      base;

    // addons from frame geometry
    boxing_pointi           frame_dimension;
    boxing_pointi           corner_mark_dimension;
    int                     corner_mark_gap_size;
    boxing_pointi           contaner_dimension;
    frame_reference_bars    reference_bars;
    DBOOL                   vertical_border_tracking; // unused ?
    boxing_float            avg_max;
    boxing_float            avg_min;
} boxing_tracker_gpf_b1;


//============================================================================

boxing_tracker_gpf_b1 * boxing_frame_tracker_gpf_b1_create(boxing_frame_gpf_b1 * generic_frame);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // DFRAMETRACKERGPF_B1_H
