#ifndef FILTER_H
#define FILTER_H

/*****************************************************************************
**
**  Definition of the filter C interface
**
**  Creation date:  2015/09/02
**  Created by:     Morhun Dmytro
**
**
**  Copyright (c) 2015 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

#ifdef __cplusplus
extern "C" {
#endif

//  PROJECT INCLUDES
//
#include "image8.h"


enum boxing_filter_callback_result
{
    BOXING_FILTER_CALLBACK_OK = 0,
    BOXING_FILTER_CALLBACK_ERROR,
    BOXING_FILTER_CALLBACK_ABORT
};

typedef struct boxing_filter_coeff_2d_s
{
    int rows;
    int cols;
    boxing_float *coeff;
} boxing_filter_coeff_2d;

typedef int (*boxing_filter_callback)(void * user, boxing_image8 * image, const boxing_float* coeff, int filter_size);

typedef struct boxing_filter_s
{
    boxing_filter_callback    process;
    boxing_filter_coeff_2d *  coeff;
} boxing_filter;


void               boxing_filter_init(boxing_filter * filter);
void               boxing_filter_free(boxing_filter * filter);
void               boxing_filter_coefficients_init( boxing_filter_coeff_2d * filter_coeff, int rows, int cols, boxing_float *coefficients );

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif // FILTER_H
