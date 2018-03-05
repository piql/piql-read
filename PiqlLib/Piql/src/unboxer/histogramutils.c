/*****************************************************************************
**
**  Implementation of the histogram utils interface
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
#include <Piql/inc/boxing/platform/platform.h>
#include "boxing/unboxer/histogramutils.h"
#include "boxing/platform/memory.h"
#include "boxing/math/math.h"

// PUBLIC HISTOGRAM UTILS FUNCTIONS
//

boxing_histogram boxing_histogram_create()
{
    boxing_histogram utils = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY_CLEAR(int, BOXING_HISTOGRAM_SIZE);
    return utils;
}

void boxing_histogram_free(boxing_histogram histoutils)
{
    boxing_memory_free(histoutils);
}

void   boxing_histogram_reset(boxing_histogram utils)
{
    memset(utils, 0, BOXING_HISTOGRAM_SIZE * sizeof(int));
}

void   boxing_histogram_calc_hist(boxing_histogram utils, const boxing_image8 * image, const boxing_pointi *point, const boxing_pointi *dimension)
{
    boxing_histogram_reset(utils);

    int y_end = (point->y + dimension->y) < (int)image->height ? (point->y + dimension->y) : (int)image->height;
    int x_end = (point->x + dimension->x) < (int)image->width ? (point->x + dimension->x) : (int)image->width;

    for (int y = point->y; y < y_end; y++)
    {
        const boxing_image8_pixel *scan_line = IMAGE8_PPIXEL(image, point->x, y);
        for (int x = point->x; x < x_end; x++)
        {
            utils[*scan_line++]++;
        }
    }
}

void   boxing_histogram_equalize(boxing_histogram histogram, int * lut, boxing_float saturation)
{
    int min = BOXING_PIXEL_MIN;
    int max = BOXING_PIXEL_MAX;

    boxing_histogram_calc_limits(histogram, &min, &max, saturation);

    int index = 0;
    for(;index < min; index++)
    {
        lut[index] = BOXING_PIXEL_MIN;
    }

    for(; index < max + 1; index++)
    {
        lut[index] = (int)floorf(BOXING_PIXEL_MAX*(boxing_float)(index - min)/(boxing_float)(max - min) + 0.5f);
    }

    for(;index < BOXING_HISTOGRAM_SIZE; index++)
    {
        lut[index] = BOXING_PIXEL_MAX;
    }
}

void   boxing_histogram_calc_limits( boxing_histogram histogram, int * min, int * max, boxing_float saturation/* = 0.0*/)
{
    *min = BOXING_PIXEL_MIN;
    *max = BOXING_PIXEL_MAX;

    boxing_float total = 0.0f;
    boxing_float saturation_level;
    for(int unsigned i = 0; i < BOXING_HISTOGRAM_SIZE; i++)
    {
        total += histogram[i];
    }

    saturation_level = 0.0f;
    for(int unsigned i = 0; i < BOXING_HISTOGRAM_SIZE; i++)
    {
        saturation_level += histogram[i];
        if(saturation_level > (total * saturation))
        {
            *min = i;
            break;
        }
    }

    saturation_level = 0.0f;
    for(int i = BOXING_HISTOGRAM_SIZE-1; i > -1; i--)
    {
        saturation_level += histogram[i];
        if(saturation_level > (total * saturation))
        {
            *max = i;
            break;
        }
    }
}

// Get the average between the minimum and the maximum level in the histogram
int    boxing_histogram_calc_measure(boxing_histogram histogram)
{
    int minimum = -1;
    int maximum = -1;

    for (int i = 0; i<BOXING_HISTOGRAM_SIZE && minimum == -1; i++)
    {
        if (histogram[i] > 0)
        {
            minimum = i;
        }
    }

    for (int i = BOXING_HISTOGRAM_SIZE-1; i>-1 && maximum == -1; i--)
    {
        if (histogram[i] > 0) 
        {
            maximum = i;
        }
    }

    return (minimum + maximum)/2;
}

int boxing_histogram_calc_measure_minmax(boxing_histogram histogram, int *minimum, int *maximum)
{
    *minimum = -1;
    *maximum = -1;
    for ( int i = 0; i < BOXING_HISTOGRAM_SIZE; i++ )
    {
        if ( histogram[i] > 0 )
        {
            *minimum = i;
            break;
        }
    }

    for ( int i = BOXING_HISTOGRAM_SIZE - 1; i > -1; i-- )
    {
        if ( histogram[i] > 0 )
        {
            *maximum = i;
            break;
        }
    }

    return (*minimum + *maximum)/2;
}

int boxing_histogram_calc_measure_full(boxing_histogram utils, const boxing_image8 * image, boxing_pointi *point, boxing_pointi *dimension, int *minimum, int *maximum)
{
    boxing_histogram_calc_measure_image(utils, image, point, dimension);
    return boxing_histogram_calc_measure_minmax(utils, minimum, maximum);
}

int boxing_histogram_calc_measure_image(boxing_histogram utils, const boxing_image8 * image, boxing_pointi *point, boxing_pointi *dimension)
{
    boxing_histogram_reset(utils);
    boxing_histogram_calc_hist(utils, image, point, dimension);
    return boxing_histogram_calc_measure(utils);
}

boxing_float boxing_histogram_calc_average(boxing_histogram histogram)
{
    int element_in_sample = 0;
    int accumulate_ample = 0;
    for (int i = 0; i < BOXING_HISTOGRAM_SIZE; i++)
    {
        accumulate_ample += (histogram[i] * i);
        element_in_sample += histogram[i];
    }
    return (boxing_float)accumulate_ample / (boxing_float)element_in_sample;
}

boxing_float boxing_histogram_calc_average_image(const boxing_image8 * image, boxing_pointi *point, boxing_pointi *dimension)
{
    long long sum = 0;
    int y_end = (point->y + dimension->y) < (int)image->height ? (point->y + dimension->y) : (int)image->height;
    int x_end = (point->x + dimension->x) < (int)image->width ? (point->x + dimension->x) : (int)image->width;

    for (int y = point->y; y < y_end; y++)
    {
        const boxing_image8_pixel *scan_line = IMAGE8_PPIXEL(image, point->x, y);//image.data(point->x,y);
        for (int x = point->x; x < x_end; x++)
        {
            sum += *scan_line++;
        }
    }

    return sum / (boxing_float)(dimension->x*dimension->y);
}
