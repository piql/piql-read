/*****************************************************************************
**
**  Implementation of the boxing sampler interface
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
#include    "boxing/unboxer/sampler.h"
#include    "boxing/platform/memory.h"

//  CONSTANTS
//

const char * BOXING_SAMPLE_CONTAINER_CONTENT  = "sampler.container.content";
const char * BOXING_SAMPLE_CONTAINER_METADATA = "sampler.container.metadata";
const char * BOXING_SAMPLE_CORNERMARKS        = "sampler.referencemarks";
const char * BOXING_SAMPLE_BAR_REFERENCE      = "sampler.bar.reference";
const char * BOXING_SAMPLE_BAR_CALIBRATION    = "sampler.bar.calibration";
const char * BOXING_SAMPLE_HORIZONTALSHIFTS   = "sampler.horizontalshifts";

// PUBLIC SAMPLER FUNCTIONS
//

void boxing_sampler_init(boxing_sampler * sampler, int width, int height)
{
    sampler->state = DFALSE;
    boxing_matrixf_init_in_place(&sampler->location_matrix, width, height);
    sampler->free = boxing_sampler_free;
    sampler->sample = BOXING_NULL_POINTER;
}

//----------------------------------------------------------------------------
/*!
 *  Does not free the original pointer.
 */
void boxing_sampler_free(boxing_sampler * sampler)
{
    if (sampler->location_matrix.is_owning_data)
    {
        boxing_memory_free(sampler->location_matrix.data);
    }
}

//----------------------------------------------------------------------------
/*!
 *  Frees the original pointer. Ascendant struct could be passed.
 */
void boxing_sampler_destroy(boxing_sampler * sampler)
{
    if (sampler)
    {
        sampler->free(sampler);
        boxing_memory_free(sampler);
    }
}
