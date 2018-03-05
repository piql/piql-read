#ifndef DABSTRACTFRAMEUTILC_H
#define DABSTRACTFRAMEUTILC_H

/*****************************************************************************
**
**  Definition of the abstract frame util interface
**
**  Creation date:  2014/12/01
**  Created by:     Hakon Larsson
**
**
**  Copyright (c) 2014 PIQL AS. All rights reserved.
**
**  This file is part of the boxing library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/image8.h"
#include "boxing/matrix.h"

//  BASE INCLUDES
//
#include "ghash.h"

//  SYSTEM INCLUDES
//
#include "boxing/bool.h"

typedef GHashTable boxing_frame_properties; // of <char *, char *>


//============================================================================
//  CLASS:  boxing_abstract_frame_util
typedef struct boxing_abstract_frame_util_s
{
    /**
    * Set the initial parameters for this object. This function is only executed
    * once throughout its lifetime
    */
    DBOOL (*initialize)(struct boxing_abstract_frame_util_s * util, const boxing_frame_properties * properties);

    /**
    * correct_frame_geometry function is sync points at given locations 
    * to compensate for any geometrical distortions not detected by
    * the general tracking mechanism.
    * is NULL if unsupported
    *
    * frame - super sampled frame
    * symbol_location_matrix - NxM matrix with cordinates to data symbols within a frame
    * sync_point_location_index - KxL matrix with indexs to each sync point location found in the symbol_location_matrix
    *
    * return optmized NxM location matrix
    */
    boxing_matrixf * (*correct_frame_geometry)(struct boxing_abstract_frame_util_s * util,
                                                    const boxing_image8 * frame,
                                                    boxing_matrixf * symbol_location_matrix, 
                                                    boxing_matrixi * sync_point_location_index);


    char * (*serialize)(const struct boxing_abstract_frame_util_s * util);

    void (*deserialize)(struct boxing_abstract_frame_util_s * util, const char * string);

    DBOOL (*learn_from_source_data)(struct boxing_abstract_frame_util_s * util, const boxing_image8 * source_data, const boxing_image8 * sampled_frame, DBOOL clear_old_learning/* = true*/);

} boxing_abstract_frame_util;

//============================================================================



//
//===================================EOF======================================

#endif // DABSTRACTFRAMEUTILC_H
