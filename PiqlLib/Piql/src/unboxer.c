/*****************************************************************************
**
**  Implementation of the unboxing interface
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
#include    "boxing/unboxer.h"
#include    "boxing/log.h"
#include    "boxing/graphics/genericframe.h"
#include    "boxing/unboxer/abstractframeutil.h"
#include    "boxing/unboxer/unboxerv1.h"
#include    "boxing/platform/memory.h"
#include    "boxing/bool.h"

//  SYSTEM INCLUDES
//

//---------------------------------------------------------------------------- 
/*! \defgroup unbox Unboxing
 *  \brief Functions for decoding analog and digital data. 
 *
 *  The unboxing library takes sampled input images, and decodes in two steps:
 *  extract and decode. The extract step locates the frame within the image, 
 *  decodes the metadata in the bottom border of the frame, then tracks the 
 *  pixels within the frame. The tracked pixels are then quantified for 
 *  digital data. The decode step is different for analog and digital data. 
 *  Digital decode is applying the codec defined by the boxing_format. Analoge
 *  decode is applying the LUT created from the frame top border calibration 
 *  bar.
 * 
 *  DEFINITIONS
 *
 *  \li \c sampled-image 2 dimensional digitized version of image stored on analog 
 *                 storage medium. Must be sampled with higher resoultion 
 *                 than original resolution used when writing the image.
 *  \li \c raw-image 2 dimensional digital image to be written on analog storage
 *                 medium. The image represents a rendered 2D barcode image
 *                 with a frame and a data container. The resoultion of the
 *                 raw image per printed pixel is from1 to 8 bits.
 *  \li \c metadata Generic information stored in the border of the frame of
 *                 a raw image. Examples can be frame number and checksums.
 *  \li \c boxing Coding analog and digital data into raw images.
 *  \li \c unboxing Decoding sampled images and restoring the original content
 *                 written to the raw image.
 *  \li \c boxing-format Parameters describing the raw image geometry and the methods 
 *                 used for coding the digital data into the frame.
 *  \li \c extract-step First step in unboxing a sampled-image. The process consists 
 *                 of locating the frame within the image, then decoding the metadata 
 *                 in the bottom border of the frame, then tracking the pixels within 
 *                 the frame. The tracked pixels are then quantified for digital data.
 *  \li \c decode-step Second step in unboxing a sampled-image. The step is different 
 *                 for analog and digital data. Digital decode is applying the codec 
 *                 defined by the boxing_format. Analoge decode is applying the LUT 
 *                 created from the frame top border calibration bar.
 */


//---------------------------------------------------------------------------- 
/*! \struct boxing_decode_filter_param_s unboxer.h
 *  \brief Codec decoding step.
 *  \ingroup unbox
 *
 *  \var name      Decoding step name, NULL if last step in chain.
 *  \var callback  Decoding function
 *
 *  Defines a decoding step in the decoding chain.
 */


//---------------------------------------------------------------------------- 
/*! \struct  boxing_unbox_parameters_s unboxer.h
 *  \brief   Configuration for the unboxer. 
 *  \ingroup unbox
 * 
 *  \var is_raw                     true if input data is in RAW format.
 *  \var training_mode              true if unboxer should be run in training mode.
 *  \var training_mode_reference    Original RAW frame.
 *  \var pre_filter                 5x5 sharpness filter that will be executed on 
 *                                  images to be unboxed. Default filter function is used 
 *                                  if set to NULL. Provide a direct assignemement function 
 *                                  (i.e. destination = source) if you want to skip 
 *                                  filtering.
 *  \var find_corner_marks          Callback that can be used to override default corner 
 *                                  mark detection. If NULL default implementation will be 
 *                                  used.
 *  \var decoding_filters           Codec decoding functions.
 *  \var format                     Boxing format, defines frame geometry and codecs.
 *  \var training_result            Output from training mode.
 *
 *  Configure unboxer.
 *
 *  Note 1: Training mode is an experimental feature for improving the unboxer
 *          by unboxing a known frame and using it's characteristics when decoding
 *          the rest of the frames. Could be removed in future versions in the
 *          library.
 *  Note 2: Pre-filtering of the image is really the responsibility of the reading
 *          device. This function will be removed in future versions of the library.
 *
 */ 


//----------------------------------------------------------------------------
/*!
 *  \struct  boxing_unboxing_codec_info_s  unboxer.h 
 *  \brief   Codec info
 *  \ingroup unbox
 *
 *  \var name       Name of the codec.
 *  \var reentrant  Codec parameter.
 *
 *  The struct with information about codec.
 */

// PUBLIC UNBOXER FUNCTIONS
//

//---------------------------------------------------------------------------- 
/*! \brief Create unboxer
 * 
 *  Create unboxer with given parameters.
 *
 *  \ingroup unbox
 *  \param parameter  Unboxing configuration.
 */

boxing_unboxer * boxing_unboxer_create(boxing_unboxer_parameters * parameters)
{
    if(!parameters)
    {
        return NULL;
    }

    boxing_dunboxerv1 * unboxer = boxing_dunboxerv1_create();
    if(!unboxer)
    {
        return NULL;
    }

    // Free dynamically allocated memory before copy parameters
    boxing_memory_free(unboxer->parameters.pre_filter.coeff);

    unboxer->parameters = *parameters;

    if (parameters->training_result)
    {
        boxing_abstract_frame_util * frame_util = unboxer->frame_util;
        if (frame_util)
        {
            frame_util->deserialize(frame_util, (char *)parameters->training_result);
        }
        else
        {
            DLOG_WARNING( "boxing_unbox:  No frame util set.  Loading traing result file aborted" );
        }
    }

    if (boxing_dunboxerv1_setup_config(unboxer) != BOXING_UNBOXER_OK)
    {
        boxing_dunboxerv1_destroy(unboxer);
        return NULL;
    }

    unboxer->metadata_codec = boxing_codecdispatcher_create(BOXING_VIRTUAL2(unboxer->frame, metadata_container, capasity),
                                        BOXING_CODEC_MODULATION_PAM2, unboxer->parameters.format, "MetadataCodingScheme");
    boxing_codecdispatcher_callback_setup(unboxer->metadata_codec, unboxer->parameters.codec_cb);

    int levels_per_symbol = unboxer->frame->levels_per_symbol(unboxer->frame);

    unboxer->codec = boxing_codecdispatcher_create(BOXING_VIRTUAL2(unboxer->frame, container, capasity), levels_per_symbol, unboxer->parameters.format, "DataCodingScheme");
    boxing_codecdispatcher_callback_setup(unboxer->codec, unboxer->parameters.codec_cb);
    boxing_codecdispatcher_reset(unboxer->codec);
    return (boxing_unboxer *)unboxer;
}


//---------------------------------------------------------------------------- 
/*! \brief Free unboxer data
 * 
 *  Free data owned by unboxer structure, but not structure itself.
 *
 *  \ingroup unbox
 *  \param unboxer Unboxer instance.
 */

void boxing_unboxer_free(boxing_unboxer * unboxer)
{
    boxing_dunboxerv1 * ub = (boxing_dunboxerv1 *)unboxer;
    if(ub)
    {
        boxing_codecdispatcher_free(ub->codec);
        boxing_codecdispatcher_free(ub->metadata_codec);
        boxing_dunboxerv1_destroy(ub);
    }
}


//---------------------------------------------------------------------------- 
/*! \brief Decode image
 * 
 *  Decode image and return data and metadata on success.
 *
 *  \ingroup unbox
 *  \param[out] data         Decoded data.
 *  \param[out] metadata     Decoded metadata.
 *  \param[in]  image        Input image.
 *  \param[in]  unboxer      Unboxer structure.
 *  \param[in]  user_data    User data.
 *  \return     Result of the decode image.
 */

enum boxing_unboxer_result boxing_unboxer_unbox(gvector * data, boxing_metadata_list * metadata, boxing_image8 * image, boxing_unboxer * unboxer, int * extract_result, void *user_data)
{
	return (enum boxing_unboxer_result)boxing_dunboxerv1_process((boxing_dunboxerv1 *)unboxer, data, metadata, image, extract_result, user_data);
}


//----------------------------------------------------------------------------
/*!
 *  \brief   Get the codec information.
 *
 *  Get the information of the current codec
 *  from the unboxer instance.
 *
 *  \ingroup unbox
 *  \param[in]  unboxer  Unboxer instance.
 *  \param[in]  info     Codec information.
 */

void boxing_unboxer_codec_info(const boxing_unboxer * unboxer, int step, boxing_codec_info *info)
{
    boxing_dunboxerv1 * ub = (boxing_dunboxerv1 *)unboxer;
    if(ub && (step > -1) && (step < (int)ub->codec->decode_codecs.size))
    {
        boxing_codec * codec = GVECTORN(&ub->codec->decode_codecs, boxing_codec *, step);
        info->name      = codec->name;
        info->reentrant = codec->reentrant;
    }
}


//----------------------------------------------------------------------------
/*!
 *  \brief   Get the number of decoding steps.
 *
 *  Get the number of decoding steps from the unboxer instance.
 *  Returns zero if we have no instance of unboxer.
 *
 *  \ingroup unbox
 *  \param[in]  unboxer  Unboxer instance.
 *  \return     number of decoding steps.
 */

size_t boxing_unboxer_decoding_steps(const boxing_unboxer * unboxer)
{
    if(!unboxer)
    {
        return 0;
    }

    boxing_dunboxerv1 * ub = (boxing_dunboxerv1 *)unboxer;
    return ub->codec->decode_codecs.size;
}


//----------------------------------------------------------------------------
/*!
 *  \brief   Reset the unboxer.
 *
 *  Reset codec and metadata codec in the unboxer instance.
 *
 *  \ingroup unbox
 *  \param[in]  unboxer  Unboxer instance.
 */

void boxing_unboxer_reset(const boxing_unboxer * unboxer)
{
    if(unboxer)
    {
        boxing_dunboxerv1 * ub = (boxing_dunboxerv1 *)unboxer;
        boxing_codecdispatcher_reset(ub->codec);
        boxing_codecdispatcher_reset(ub->metadata_codec);
    }
}


//----------------------------------------------------------------------------
/*!
 *  \brief   Extract data container.
 *
 *  Unbox metadata and extract a raw quantized copy of the data container.
 *
 *  \ingroup unbox
 *  \param[out] data      Quantized data container.
 *  \param[out] metadata  Decoded metadata.
 *  \param[in]  image     Image to be decoded.
 *  \param[in]  unboxer   Unboxer structure.
 *  \param[in]  user_data User data.
 *  \return Unboxing result status code
 */

enum boxing_unboxer_result boxing_unboxer_unbox_extract_container(
                    gvector * data,
                    boxing_metadata_list * metadata,
                    boxing_image8 * image, 
                    boxing_unboxer * unboxer,
                    void *user_data)
{
    return (enum boxing_unboxer_result)boxing_dunboxerv1_extract_container((boxing_dunboxerv1 *)unboxer, data, metadata, image, user_data);
}


//----------------------------------------------------------------------------
/*!
 *  \brief   Decode data.
 *
 *  Unbox metadata and extract a raw quantized copy of the data container.
 *
 *  \ingroup unbox
 *  \param[in]  unboxer       Unboxer structure
 *  \param[in/out] data       Quantized data container.
 *  \param[in] metadata       Decoded metadata
 *  \param[out] decode_stats  Statistic of the decode process
 *  \param[in] step           Decoding step
 *  \param[in] user_data      User data.
 *  \return Unboxing result status code
 */

int boxing_unboxer_decode(boxing_unboxer * unboxer, gvector * data, boxing_metadata_list * metadata,  boxing_stats_decode * decode_stats, unsigned int step, void *user_data)
{
    return boxing_dunboxerv1_decode((boxing_dunboxerv1 *)unboxer, data, metadata, decode_stats, step, user_data);
}


//---------------------------------------------------------------------------- 
/*! \brief Enable/disable unboxer for decoding RAW images.
 * 
 *  Set if input images should be interpreted as RAW images.
 *
 *  \ingroup unbox
 *  \param[in]  unboxer      Unboxer instance.
 *  \param[in]  is_raw       false if 0, else true
 */

void boxing_unboxer_set_raw_input(boxing_unboxer * unboxer, int is_raw)
{
    ((boxing_dunboxerv1 *)unboxer)->parameters.is_raw = (is_raw == 0 ? 0 : 1);
}


//---------------------------------------------------------------------------- 
/*! \brief Get RAW mode.
 * 
 *  Return is_raw parameter.
 *
 *  \ingroup unbox
 *  \param[in]  unboxer      Unboxer instance.
 *  \return is_raw value.
 */

int boxing_unboxer_is_raw_input(boxing_unboxer * unboxer)
{
    return ((boxing_dunboxerv1 *)unboxer)->parameters.is_raw;
}


//---------------------------------------------------------------------------- 
/*! \brief Initialize unboxer parameters
 * 
 *  Initialize unboxer parameters to default values.
 *
 *  \ingroup unbox
 *  \param[in]  parameters      Parameters to be initialized.
 */

void boxing_unboxer_parameters_init(boxing_unboxer_parameters * parameters)
{
#ifdef BOXINGLIB_CALLBACK
    parameters->on_tracker_created = NULL;
    parameters->on_content_sampled = NULL;
    parameters->on_content_quantized = NULL;
    parameters->on_metadata_complete = NULL;
    parameters->on_reference_bar_complete = NULL;
    parameters->on_corner_mark_complete = NULL;
    parameters->on_training_complete = NULL;
    parameters->on_decode_step = NULL;
    parameters->on_all_complete = NULL;
    parameters->orig_image = NULL;
#endif
    parameters->format = NULL;
    parameters->codec_cb = NULL;
    parameters->is_raw = DFALSE;
    parameters->training_mode = DFALSE;
    parameters->training_result = NULL;
    parameters->sample_contents = NULL;
    parameters->quantize_contents = NULL;
    boxing_filter_init( &parameters->pre_filter );
}


//---------------------------------------------------------------------------- 
/*! \brief Free unboxing parameters data.
 * 
 *  Free data owned by the parameters, but not the parameter instance itself.
 *
 *  \ingroup unbox
 *  \param[in]  parameters      Parameters to be freed.
 */

void boxing_unboxer_parameters_free(boxing_unboxer_parameters * parameters)
{
    boxing_filter_free( &parameters->pre_filter );
}


boxing_codecdispatcher *  boxing_unboxer_dispatcher(boxing_unboxer * unboxer, const char * coding_scheme)
{
    return boxing_dunboxerv1_dispatcher(unboxer, coding_scheme);
}
