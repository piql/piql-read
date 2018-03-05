/*****************************************************************************
**
**  Implementation of the frame utility interface
**
**  Creation date:  2016/06/28
**  Created by:     Piql
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "frameutil.h"
#include "boxing/utils.h"
#include "boxing/platform/memory.h"

//  PRIVATE INTERFACE
//

static DBOOL learn_from_source_data(struct boxing_abstract_frame_util_s * util, const boxing_image8 * source_data, const boxing_image8 * sampled_frame, DBOOL clear_old_learning);
static DBOOL abstract_frame_util_initialize(struct boxing_abstract_frame_util_s * util, const boxing_frame_properties * properties);

// PUBLIC FRAME UTIL FUNCTIONS
//

boxing_frame_util * boxing_frame_util_create()
{
    boxing_frame_util * util = BOXING_MEMORY_ALLOCATE_TYPE(boxing_frame_util);
    util->base.correct_frame_geometry = NULL;
    util->base.initialize = abstract_frame_util_initialize;
    util->base.learn_from_source_data = learn_from_source_data;
    return util;
}

//----------------------------------------------------------------------------
/*!
 *  This frees the initial pointer.
 */
void boxing_frame_util_destroy(boxing_abstract_frame_util * util)
{
    boxing_memory_free(util);
}

//====================================================================================================
//
// Public methods implementing DAbstractFrameUtil
//
//====================================================================================================


// PRIVATE FRAME UTIL FUNCTIONS
//

//
// initialize
// ----------
/**
 * Set the initial parameters for this object. This function is only executed
 * once throughout its lifetime
 * @param properties The initialization properties
 * @return true when operation succeeded, false if not
 */
static DBOOL abstract_frame_util_initialize(struct boxing_abstract_frame_util_s * util, const boxing_frame_properties * properties)
{
    BOXING_UNUSED_PARAMETER( util );
    BOXING_UNUSED_PARAMETER( properties );

    return DTRUE;
}


//
// learn_from_source_data
// -------------------
/**
 * It is learned, which source values are assigned to the sampled values and for which sampled values the
 * pattern decoding algorithm shall be used.
 * @param source_data The related source data used on the encoding side
 * @param sourceBorderValue The source value of the border around the data
 * @param sampled_frame The scanned and sampled frame
 * @param surroundingLines The number of surrounding lines around the data area (1 is needed if pattern decoding shall be used)
 * @param clear_old_learning If true (default), the results of old learnings are cleared. If false, the old values are enhanced
 * @return true if the operation succeeded, false if not
 */
static DBOOL learn_from_source_data(
    struct boxing_abstract_frame_util_s * util, 
    const boxing_image8 * source_data, 
    const boxing_image8 * sampled_frame, 
    DBOOL clear_old_learning )
{
    BOXING_UNUSED_PARAMETER( clear_old_learning );
    BOXING_UNUSED_PARAMETER( sampled_frame );
    BOXING_UNUSED_PARAMETER( source_data );
    BOXING_UNUSED_PARAMETER( util );
    
    return DTRUE;
}

