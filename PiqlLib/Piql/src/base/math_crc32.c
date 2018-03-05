/*****************************************************************************
**
**  Implementation of the crc32 interface
**
**  Creation date:  2013/08/20
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/math/crc32.h"
#include "boxing/log.h"
#include "boxing/platform/memory.h"

struct dcrc32_s
{
    boxing_uint32 crc;
    boxing_uint32 CRCTable[256];
};

// PUBLIC MATH CRC32 FUNCTIONS
//

dcrc32 * boxing_math_crc32_create_def()
{
    return boxing_math_crc32_create(0, POLY_CRC_32C);
}

dcrc32 * boxing_math_crc32_create(boxing_uint32 seed, boxing_uint32 polynom)
{
    dcrc32 * dcrc = BOXING_MEMORY_ALLOCATE_TYPE(dcrc32);
    DFATAL(dcrc, "Out of memory");

    dcrc->crc = seed;

    // precalculate crc32 for 8 bit data
    for (unsigned int i = 0; i < 256; i++) 
    {
        int j;
        boxing_uint32 part = ((unsigned long long)i << 24);
        for (j = 0; j < 8; j++) 
        {
            if ((part) & 1UL<<31)
                part = (part << 1) ^ polynom;
            else
                part <<= 1;
        }
        dcrc->CRCTable[i] = part;
    }

    return dcrc;
}

void boxing_math_crc32_free(dcrc32 *dcrc32)
{
    if (dcrc32)
        boxing_memory_free(dcrc32);
}

boxing_uint32 boxing_math_crc32_calc_crc(dcrc32 * dcrc32, const char *data, unsigned int size)
{
    return (dcrc32->crc = boxing_math_crc32_calc_crc_re(dcrc32, dcrc32->crc, data, size));
}

boxing_uint32 boxing_math_crc32_calc_crc_re(const dcrc32 * dcrc32, boxing_uint32 seed, const char *data, unsigned int size)
{
    boxing_uint32 crc = seed;
    while(size)
    {
        crc = (crc << 8) ^ dcrc32->CRCTable[((crc >> 24) ^ *data) & 0xff];
        data++;
        size--;
    }
    return crc;
}

boxing_uint32 boxing_math_crc32_get_crc(dcrc32 * dcrc32)
{
    return dcrc32->crc;
}

void boxing_math_crc32_reset(dcrc32 * dcrc32, boxing_uint32 seed)
{
    dcrc32->crc = seed;
}


/********************************** EOF *************************************/
