/*****************************************************************************
**
**  Implementation of the modulator codec interface
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
#include <math.h>
#include "boxing/codecs/2dpam.h"
#include "boxing/log.h"
#include "boxing/platform/memory.h"
#include "boxing/image8.h"
#include "horizontalmeasures.h"

//  DEFINES
//

#define CODEC_MEMBER(name) (((boxing_codec_modulator *)codec)->name)
#define CODEC_BASE_MEMBER(name) (((boxing_codec_modulator *)codec)->base.name)

//  CONSTANTS
//

typedef unsigned char pam_symboli;
typedef unsigned char pam_symbolf;

typedef struct pam2d_symboli_s
{
    pam_symboli s0;
    pam_symboli s1;
}pam2d_symboli;


/*  32 2D-PAM mapping
 * 
 *   s1 (y)
 *   ^
 *   |
 * 5 +    10000  10001  10011  11011  11001  11000
 *   |
 * 4 +    10100         10010  11010         11100
 *   |
 * 3 +    10101  10111  10110  11110  11111  11101
 *   |
 * 2 +    00101  00111  00110  01110  01111  01101
 *   |                                            
 * 1 +    00100         00010  01010         01100
 *   |                                            
 * 0 +    00000  00001  00011  01011  01001  01000
 *   |                                            
 *    ------|------|------|------|------|------|-----> s0 (x)
 *          0      1      2      3      4      5    
 */

static const pam2d_symboli map_32_2dPam[32] =
{
    { 0, 0 }, { 1, 0 }, { 2, 1 }, { 2, 0 }, { 0, 1 }, { 0, 2 }, { 2, 2 }, { 1, 2 },

    { 5, 0 }, { 4, 0 }, { 3, 1 }, { 3, 0 }, { 5, 1 }, { 5, 2 }, { 3, 2 }, { 4, 2 },

    { 0, 5 }, { 1, 5 }, { 2, 4 }, { 2, 5 }, { 0, 4 }, { 0, 3 }, { 2, 3 }, { 1, 3 },

    { 5, 5 }, { 4, 5 }, { 3, 4 }, { 3, 5 }, { 5, 4 }, { 5, 3 }, { 3, 3 }, { 4, 3 }
};

/*  8 2D-PAM mapping
*
*   s1 (y)
*   ^
*   |
* 2 +     101    111    110  
*   |                   
* 1 +     100           010  
*   |                   
* 0 +     000    001    011  
*   |
*    ------|------|------|------> s0 (x)
*          0      1      2    
*/
static const pam2d_symboli map_8_2dPam[8] =
{
    { 0, 0 }, { 1, 0 }, { 2, 1 }, { 2, 0 }, { 0, 1 }, { 0, 2}, { 2, 2 }, { 1, 2 }
};

/*  4 2D-PAM mapping
*
*   s1 (y)
*   ^
*   |
* 1 +      10     11
*   |             
* 0 +      00     01
*   |
*    ------|------|------> s0 (x)
*          0      1
*/
static const pam2d_symboli map_4_2dPam[4] =
{
    { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 }
};

//  PRIVATE INTERFACE
//

static DBOOL codec_decode(void * codec, gvector * data, gvector * erasures, boxing_stats_decode * stats, void* user_data);
static DBOOL codec_encode(void * codec, gvector * data);

// PUBLIC MODULATOR FUNCTIONS
//


static boxing_codec_syncpointinserter * create_syncpointinserter(GHashTable * properties, const boxing_config * config)
{
    const g_variant * value;
    value = boxing_config_property_const(config, "FrameFormat", "syncPointHDistance");
    if (value)
        g_hash_table_replace(properties, boxing_string_clone("SyncPointHDistancePixel"), g_variant_clone(value));

    value = boxing_config_property_const(config, "FrameFormat", "syncPointVDistance");
    if (value)
        g_hash_table_replace(properties, boxing_string_clone("SyncPointVDistancePixel"), g_variant_clone(value));

    value = boxing_config_property_const(config, "FrameFormat", "syncPointRadius");
    if (value)
        g_hash_table_replace(properties, boxing_string_clone("SyncPointRadiusPixel"), g_variant_clone(value));

    value = boxing_config_property_const(config, "FrameFormat", "maxLevelsPerSymbol");
    if (value)
    {
        unsigned int levels_per_symbol = g_variant_to_uint(value);
        unsigned int bits_per_symbol = 0;
        while (levels_per_symbol)
        {
            bits_per_symbol++;
            levels_per_symbol >>= 1;
        }

        g_hash_table_replace(properties, boxing_string_clone("NumBitsPerPixel"), g_variant_create_uint(bits_per_symbol));
    }

    g_hash_table_replace(properties, boxing_string_clone("DataOrientation"), g_variant_create_uint(1));    

    return (boxing_codec_syncpointinserter *)boxing_codec_syncpointinserter_create(properties, config);

}

boxing_codec * boxing_codec_2dpam_create(GHashTable * properties, const boxing_config * config)
{
    BOXING_UNUSED_PARAMETER( config );
    boxing_codec_2dpam * codec = BOXING_MEMORY_ALLOCATE_TYPE(boxing_codec_2dpam);
    boxing_codec_init_base((boxing_codec *)codec);

    codec->syncpointinserter = create_syncpointinserter(properties, config);
    codec->syncpointinserter->property_value_sync_point_foreground_m = 5;

    codec->base.free = boxing_codec_2dpam_free;
    codec->base.is_error_correcting = DFALSE;
    codec->base.name = codec_2dpam_name;
    codec->base.decode = codec_decode;
    codec->base.encode = codec_encode;

    codec->base.encoded_symbol_size = codec->syncpointinserter->base.encoded_symbol_size;
    codec->base.decoded_symbol_size = 5;

    codec->base.encoded_block_size = codec->syncpointinserter->base.encoded_block_size;
    codec->base.decoded_block_size = codec->syncpointinserter->base.decoded_block_size / 2;

    return (boxing_codec *)codec;
}

void boxing_codec_2dpam_free(boxing_codec *codec)
{
    boxing_codec_syncpointinserter_free((boxing_codec *)(((boxing_codec_2dpam *)codec)->syncpointinserter));
    boxing_codec_release_base(codec);
    boxing_memory_free(codec);
}

// PRIVATE MODULATOR FUNCTIONS
//
struct symbol_tracker
{
    int   width;
    int   height;
    int   y;
    int   x;
    char *img_cur;
    char *map_bg;
    char *map_bg_cur;
    char *map_bg_end;
    char *map_fg_cur;
};

static DBOOL get_next_symbol(struct symbol_tracker * tracker)
{
    while (tracker->map_bg_cur != tracker->map_bg_end)
    {
        tracker->map_bg_cur++, tracker->img_cur++, tracker->map_fg_cur++;

        if (!(*tracker->map_bg_cur || *tracker->map_fg_cur))
        {
            ptrdiff_t pos = tracker->map_bg_cur - tracker->map_bg;
            tracker->y = (int)(pos / tracker->width);
            tracker->x = (int)(pos - (tracker->y * tracker->width));
            return DTRUE;
        }
    }

    return DFALSE;    
}


static void init_tracker(struct symbol_tracker *tracker, boxing_codec_syncpointinserter * codec, char *img)
{
    tracker->map_bg = codec->bitarray_sync_point_background;
    tracker->map_bg_cur = codec->bitarray_sync_point_background-1;
    tracker->map_fg_cur = codec->bitarray_sync_point_foreground-1;
    tracker->width = codec->property_image_size_m.x;
    tracker->height = codec->property_image_size_m.y;

    tracker->map_bg_end = tracker->map_bg + (tracker->width * tracker->height);
    tracker->x = -1;
    tracker->y = -1;
    tracker->img_cur = img-1;
}
static DBOOL codec_decode(void * codec, gvector * data, gvector * erasures, boxing_stats_decode * stats, void* user_data)
{
    BOXING_UNUSED_PARAMETER(stats);
    BOXING_UNUSED_PARAMETER(erasures);
    BOXING_UNUSED_PARAMETER(user_data);
    boxing_image8 image;
    
    image.width = ((boxing_codec_2dpam *)codec)->syncpointinserter->property_image_size_m.x;
    image.height = ((boxing_codec_2dpam *)codec)->syncpointinserter->property_image_size_m.y;
    image.is_owning_data = DFALSE;
    image.data = data->buffer;
    boxing_pointi block_size = { 32, 32 };
    boxing_matrix_float * means = boxing_calculate_means(&image, block_size.x, block_size.y, 6);

    // demodulate
    struct symbol_tracker tracker;

    init_tracker(&tracker, ((boxing_codec_2dpam *)codec)->syncpointinserter, data->buffer);

    gvector * decoded_data = gvector_create(data->item_size, ((boxing_codec_2dpam *)codec)->base.decoded_block_size);

    unsigned char * dst = (char *)decoded_data->buffer;

    while (1)
    {
        if (DTRUE != get_next_symbol(&tracker))
            break;
        int s0 = *(unsigned char*)tracker.img_cur;
        boxing_float *mean0 = MATRIX_MULTIPAGE_ROW_PTR(means, tracker.x/block_size.x, tracker.y/block_size.y);

        if (DTRUE != get_next_symbol(&tracker))
            break;
        int s1 = *(unsigned char*)tracker.img_cur;
        boxing_float *mean1 = MATRIX_MULTIPAGE_ROW_PTR(means, tracker.x / block_size.x, tracker.y / block_size.y);

        boxing_float min_distance = BOXING_FLOAT_MAX;

        for (unsigned char j = 0; j < 32; j++)
        {
            pam2d_symboli symbol = map_32_2dPam[j];

            boxing_float delta_s0 = mean0[symbol.s0] - s0;
            boxing_float delta_s1 = mean1[symbol.s1] - s1;
            boxing_float distance = sqrtf(delta_s0*delta_s0 + delta_s1*delta_s1);
            if (distance < min_distance)
            {
                min_distance = distance;
                (*dst) = j;
            }
        }
        dst++;
    }

    gvector_swap(data, decoded_data);
    gvector_free(decoded_data);
    return DTRUE;
}

static DBOOL codec_encode(void * codec, gvector * data)
{
    gvector * encoded_data = gvector_create(data->item_size, ((boxing_codec*)codec)->encoded_block_size);

    //
    unsigned char * src = (char *)data->buffer;
    unsigned char * dst = (char *)encoded_data->buffer;
    for (unsigned int i = 0; i < data->size; i++, dst++, src++)
    {
        pam2d_symboli sympols = map_32_2dPam[0x1f & (*src)];

        (*dst) = (unsigned char)sympols.s0;
        dst++;
        (*dst) = (unsigned char)sympols.s1;
    }

    // insert sync

    gvector_swap(data, encoded_data);
    gvector_free(encoded_data);


    boxing_codec_syncpointinserter *syncpointinserter = ((boxing_codec_2dpam *)codec)->syncpointinserter;
    return ((boxing_codec *)syncpointinserter)->encode(syncpointinserter, data);
}
