#ifndef BILINEARSAMPLER_H
#define BILINEARSAMPLER_H

/*****************************************************************************
**
**  Definition of bicubic interpolation interface
**
**  Creation date:  2017/03/29
**  Created by:     Alexander Silokhin
**
**
**  Copyright (c) 2017 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

#include "../../inc/boxing/unboxer/sampler.h"

#ifdef __cplusplus
extern "C" {
#endif

//  PROJECT INCLUDES
//
//#include "boxing/unboxer/sampler.h"

boxing_sampler * boxing_bilinearsampler_create(int width, int height);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // BILINEARSAMPLER_H
