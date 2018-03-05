#ifndef CODEC_CALLBACK_H
#define CODEC_CALLBACK_H

/*****************************************************************************
**
**  Definition of the codex base functions
**
**  Creation date:  2016/11/15
**  Created by:     Piql AS
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
#include "boxing/bool.h"
#include "boxing/platform/types.h"

typedef DBOOL (*codec_encode_cb)(void *user_data, unsigned char * data_encoded, const int encoded_size, 
                        unsigned char * data_decoded, int *decoded_size);
typedef DBOOL (*codec_decode_cb)(void * user, void *user_data, unsigned char * data_encoded, const int encoded_size, unsigned char * data_decoded, 
                        int *decoded_size, int *errors_resolved, int *errors_fatal, boxing_float *fec_amount, float *fec_weight);

typedef struct boxing_codec_cb_s
{
    const char *             name; 
    codec_encode_cb          encode;
    codec_decode_cb          decode;
} boxing_codec_cb;

#ifdef __cplusplus
} /*extern "C" */
#endif

#endif /* CODEC_CALLBACK_H */

