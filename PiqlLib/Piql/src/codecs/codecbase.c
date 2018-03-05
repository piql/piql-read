/*****************************************************************************
**
**  Implementation of the codecbase interface
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
#include "boxing/codecs/codecbase.h"
#include "boxing/codecs/reedsolomon.h"
#include "boxing/codecs/interleaving.h"
#include "boxing/codecs/cipher.h"
#include "boxing/codecs/modulator.h"
#include "boxing/codecs/syncpointinserter.h"
#include "boxing/codecs/crc32.h"
#include "boxing/codecs/crc64.h"
#include "boxing/codecs/packetheader.h"
#include "boxing/codecs/ftfinterleaving.h"
#include "boxing/codecs/2dpam.h"
#include "boxing/codecs/bchcodec.h"

#include "boxing/codecs/symbolconverter.h"

// PUBLIC CODEC BASE FUNCTIONS
//

static DBOOL init_capacity(struct boxing_codec_s *codec, int size)
{
    int blocks = (size / codec->encoded_block_size);
    codec->decoded_data_size = codec->decoded_block_size * blocks;
    codec->encoded_data_size = codec->encoded_block_size * blocks;
    return DTRUE;
}

boxing_codec * boxing_codec_create(const char *codec_name, GHashTable * properties, const boxing_config * config)
{
    if (boxing_string_equal(codec_name, "Interleaving"))
    {
        return boxing_interleaving_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, "Cipher"))
    {
        return boxing_codec_cipher_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, "ReedSolomon"))
    {
        return boxing_codec_reedsolomon_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, "Modulator"))
    {
        return boxing_codec_modulator_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, "SyncPointInserter"))
    {
        return boxing_codec_syncpointinserter_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, codec_crc32_name))
    {
        return boxing_crc32_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, codec_crc64_name))
    {
        return boxing_crc64_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, codec_ftf_interleaving_name))
    {
        return boxing_ftf_interleaving_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, codec_packet_header_name))
    {
        return boxing_codec_packet_header_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, codec_2dpam_name))
    {
        return boxing_codec_2dpam_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, codec_symbol_converter_name))
    {
        return boxing_codec_symbol_converter_create(properties, config);
    }
    else if (boxing_string_equal(codec_name, codec_bch_name))
    {
        return boxing_codec_bch_create(properties, config);
    }
    return NULL;
}

void boxing_codec_init_base(boxing_codec *codec)
{
    codec->is_error_correcting = DFALSE;
    codec->pre_zero_pad_data = DTRUE;
    codec->encoded_symbol_size = 8;
    codec->decoded_symbol_size = 8;
    codec->decode_cb = NULL;
    codec->reset = NULL;
    codec->reentrant = 1;
    codec->init_capacity = init_capacity;
    codec->decoded_data_size = 1;
    codec->encoded_data_size = 1;
    codec->decoded_block_size = 1;
    codec->encoded_block_size = 1;

}

void boxing_codec_release_base(boxing_codec *codec)
{
    BOXING_UNUSED_PARAMETER( codec );
}

void boxing_codec_release(boxing_codec *codec)
{
    codec->free(codec);
}
