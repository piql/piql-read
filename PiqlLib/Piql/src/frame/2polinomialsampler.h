#ifndef D2POLINOMIALSAMPLER_H
#define D2POLINOMIALSAMPLER_H

/*****************************************************************************
**
**  Definition of the 2 polinomial sampler interface
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
#include "boxing/unboxer/sampler.h"

boxing_sampler * boxing_2polinomialsampler_create(int width, int height);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // D2POLINOMIALSAMPLER_H
