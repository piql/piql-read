/*****************************************************************************
**
**  Implementation of the filter interface
**
**  Creation date:  2014/01/03
**  Created by:     Ole Liabo
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include    "boxing/filter.h"
#include    "boxing/platform/memory.h"
//  SYSTEM INCLUDES
//


//----------------------------------------------------------------------------
/*!
 *  \struct     boxing_filter_coeff_2d_s  unboxer.h
 *  \brief      Filter coefficients storage.
 *  \ingroup    unbox
 *
 *  \var rows   Number of rows
 *  \var cols   Number of columns
 *  \var coeff  Pointer to the coefficients array
 *
 *  Structure storage of filter coefficients in the array with a specified size.
 */


//----------------------------------------------------------------------------
/*!
 *  \struct       boxing_filter_s  unboxer.h
 *  \brief        Data storage for the filter.
 *  \ingroup      unbox
 *
 *  \var process  Callback function for the filter
 *  \var coeff    Filter coefficients
 *
 *  The structure of the filter data storage: a pointer to callback function and filter coefficients.
 */


// PUBLIC FILTER FUNCTIONS
//

//---------------------------------------------------------------------------- 
/*! \brief Initialize 5x5 filter.
 * 
 *  Initialize filter with default sharpness filter. Note that the filter 
 *  coefficients are reader dependant and should be overridden with coefficients
 *  matching the reader image capture device.
 *
 *  \ingroup unbox
 *  \param[in]  filter      Filter instance.
 */

void boxing_filter_init( boxing_filter * filter )
{
    static boxing_float default_filter_coefficients[] =
    {
        -0.085390f,  0.188372f, -0.363868f,  0.188372f, -0.085390f,
         0.188372f, -0.499956f,  0.441212f, -0.499956f,  0.188372f,
        -0.363868f,  0.441212f,  1.500000f,  0.441212f, -0.363868f,
         0.188372f, -0.499956f,  0.441212f, -0.499956f,  0.188372f,
        -0.085390f,  0.188372f, -0.363868f,  0.188372f, -0.085390f 
    };

    filter->coeff = BOXING_MEMORY_ALLOCATE_TYPE( boxing_filter_coeff_2d );
    boxing_filter_coefficients_init( filter->coeff, 5, 5, default_filter_coefficients );
    filter->process = NULL;
}


//---------------------------------------------------------------------------- 
/*! \brief Free filter data.
 * 
 *  Free data owned by the filter.
 *
 *  \ingroup unbox
 *  \param[in]  filter      Filter instance.
 */

void boxing_filter_free( boxing_filter * filter )
{
    boxing_memory_free(filter->coeff);
}


//---------------------------------------------------------------------------- 
/*! \brief Initialize filter coefficients.
 * 
 *  Initialize filter coefficients.
 *
 *  \ingroup unbox
 *  \param[in]  coeff           Filter coefficients.
 *  \param[in]  rows            Number of filter rows.
 *  \param[in]  columns         Number of filter columns.
 *  \param[in]  coefficients    Array of rows*columns coefficients.
 */

void  boxing_filter_coefficients_init( boxing_filter_coeff_2d* coeff, int rows, int columns, boxing_float *coefficients )
{
    coeff->rows = rows;
    coeff->cols = columns;
    coeff->coeff = coefficients;
}
