/*****************************************************************************
**
**  Implementation of the interleaving codec interface
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
#include "config.h"
#include "boxing/codecs/interleaving.h"
#include "boxing/log.h"
#include "boxing/math/bitutils.h"
#include "boxing/platform/memory.h"

//  DEFINES
//

#define CODEC_MEMBER(name) (((boxing_codec_interleaving *)codec)->name)
#define CODEC_BASE_MEMBER(name) (((boxing_codec_interleaving *)codec)->base.name)

//  CONSTANTS
//

static const char codec_name[] = "Interleaving";

//  PRIVATE INTERFACE
//

static DBOOL codec_decode(void * codec, gvector * data, gvector * erasures, boxing_stats_decode * stats, void* user_data);
static DBOOL codec_encode(void * codec, gvector * data);

// PUBLIC INTERLEAVING FUNCTIONS
//

boxing_codec * boxing_interleaving_create(GHashTable * properties, const boxing_config * config)
{
    BOXING_UNUSED_PARAMETER( config ); 
    boxing_codec_interleaving * codec = BOXING_MEMORY_ALLOCATE_TYPE(boxing_codec_interleaving);
    boxing_codec_init_base((boxing_codec *)codec);
    codec->base.free = boxing_interleaving_free;
    codec->base.is_error_correcting = DFALSE;
    codec->base.name = codec_name;
    codec->base.decode = codec_decode;
    codec->base.encode = codec_encode;

    // interleaving distance
    g_variant * distance = g_hash_table_lookup(properties, PARAM_NAME_DISTANCE);
    if(distance == NULL)
    {
        DLOG_ERROR1( "Requires property '%s' to be set", PARAM_NAME_DISTANCE);
        boxing_interleaving_free((boxing_codec *)codec);
        return NULL;
    }

    // interleaving symbol type
    g_variant * symbol_type = g_hash_table_lookup(properties, PARAM_NAME_SYMBOL_TYPE);
    if(symbol_type == NULL)
    {
        DLOG_ERROR1( "Requires property '%s' to be set", PARAM_NAME_SYMBOL_TYPE);
        boxing_interleaving_free((boxing_codec *)codec);
        return NULL;
    }
    
    const char * symbol_type_str = g_variant_if_string(symbol_type);
    if (boxing_string_equal(symbol_type_str, PARAM_NAME_SYMBOL_TYPE_BIT))
    {
        CODEC_MEMBER(interleaving_symbol) = BOXING_INTERLEAVING_SYMBOL_BIT;
    }
    else if (boxing_string_equal(symbol_type_str, PARAM_NAME_SYMBOL_TYPE_BYTE))
    {
        CODEC_MEMBER(interleaving_symbol) = BOXING_INTERLEAVING_SYMBOL_BYTE;
    }
    else
    {
        DLOG_ERROR2("Unsupported '%s' : ", PARAM_NAME_SYMBOL_TYPE, symbol_type_str);
        boxing_interleaving_free((boxing_codec *)codec);
        return NULL;
    }

    CODEC_MEMBER(distance) = g_variant_to_uint(distance);
    return (boxing_codec *)codec;
}

void boxing_interleaving_free(boxing_codec *codec)
{
    boxing_codec_release_base(codec);
    boxing_memory_free(codec);
}


// PRIVATE INTERLEAVING FUNCTIONS
//

static void decode_byte_interleaving(boxing_codec_interleaving * codec, gvector * data, void * user_data)
{
    uint32_t distance = CODEC_MEMBER(distance);

    if (CODEC_BASE_MEMBER(decode_cb))
    {
        CODEC_BASE_MEMBER(decode_cb)(user_data, data, NULL, distance, NULL, NULL, NULL, NULL, NULL, NULL);
    }
    else
    {
        gvector * data_interleaved = gvector_create_char_no_init(data->size);

        uint32_t data_size = (uint32_t)data->size;
        char * data_pointer = (char *)data->buffer;
        char * data_interleaved_pointer_end = (char *)data_interleaved->buffer + data_size;
        for (uint32_t i = 0; i < distance; i++)
        {
            char * data_interleaved_pointer = (char *)data_interleaved->buffer + i;
            while (data_interleaved_pointer < data_interleaved_pointer_end)
            {
                *data_interleaved_pointer = *data_pointer;
                data_pointer++;
                data_interleaved_pointer += distance;
            }
        }
        gvector_swap(data, data_interleaved);
        gvector_free(data_interleaved);
    }
}

static DBOOL codec_decode(void * codec, gvector * data, gvector * erasures, boxing_stats_decode * stats, void* user_data)
{
    switch (CODEC_MEMBER(interleaving_symbol))
    {
    case BOXING_INTERLEAVING_SYMBOL_BIT:
        decode_byte_interleaving(codec, data, user_data);
        decode_byte_interleaving(codec, erasures, user_data);
        break;
    case BOXING_INTERLEAVING_SYMBOL_BYTE:
        decode_byte_interleaving(codec, data, user_data);
        decode_byte_interleaving(codec, erasures, user_data);
        break;
    default:
        return DFALSE;
    }
    stats->fec_accumulated_amount = 0;
    stats->fec_accumulated_weight = 0;
    stats->resolved_errors = 0;
    stats->unresolved_errors = 0;
    return DTRUE;
}

static void encode_bit_interleaving(boxing_codec_interleaving * codec, gvector * data)
{
    uint32_t distance = CODEC_MEMBER(distance);

    uint32_t num_bits = (uint32_t)data->size * 8;

    gvector * data_interleaved = gvector_create_char(data->size, 0);

    for (uint32_t i = 0, index_data = 0; i<distance; i++)
    {
        for (uint32_t j = i; j<num_bits; j += distance, index_data++)
        {
            if (BIT_IS_ON((uint8_t *)data->buffer, j))
            {
                BIT_SET_ON((uint8_t *)data_interleaved->buffer, index_data);
            }
        }
    }
    gvector_swap(data, data_interleaved);
    gvector_free(data_interleaved);
}

static void encode_byte_interleaving(boxing_codec_interleaving * codec, gvector * data)
{
    uint32_t distance = CODEC_MEMBER(distance);
    gvector * data_interleaved = gvector_create_char(data->size, 0);
    char * data_pointer = (char *)data->buffer;
    char * data_interleaved_pointer = (char *)data_interleaved->buffer;

    for (uint32_t i = 0, index_data = 0; i < distance; i++)
    {
        for (uint32_t j = i; j < (uint32_t)data->size; j += distance, index_data++)
        {
            data_interleaved_pointer[index_data] = data_pointer[j];
        }
    }

    gvector_swap(data, data_interleaved);
    gvector_free(data_interleaved);
}

static DBOOL codec_encode(void * codec, gvector * data)
{
    switch (CODEC_MEMBER(interleaving_symbol))
    {
    case BOXING_INTERLEAVING_SYMBOL_BIT:
        encode_bit_interleaving(codec, data);
        break;
    case BOXING_INTERLEAVING_SYMBOL_BYTE:
        encode_byte_interleaving(codec, data);
        break;
    default:
        return DFALSE;
    }
    return DTRUE;
}
