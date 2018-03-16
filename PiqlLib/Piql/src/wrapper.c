//
// Created by hcon on 14.03.18.
//Used to interface with java.
//
#include <jni.h>
#include <Piql/inc/boxing/metadata.h>
#include <Piql/inc/boxing/unboxer.h>
#include <Piql/inc/boxing/platform/memory.h>
#include "string.h"
#include "boxing/config.h"
#include "boxing_config.h"

JNIEXPORT void JNICALL
Java_no_ntnu_bachelor2018_filmreader_PiqlLib_Wrapper_process(JNIEnv *env, jobject instance,
                                                              jint width, jint height,
                                                              jbyteArray image_) {
    jbyte *image = (*env)->GetByteArrayElements(env, image_, NULL);

    int extract_result;
    boxing_unboxer * unboxer = boxing_get_boxing_config(format_name);
    //boxing_unboxer_unbox(gvector * data, boxing_metadata_list * metadata, boxing_image8 * image, boxing_unboxer * unboxer, int * extract_result, void *user_data)

    boxing_float filterCoeffAllPass[1][1] = { { 1.0000000 }, };
    utility->unboxer =
    // setup the unboxer
    boxing_unboxer_parameters unbox_params;
    boxing_unboxer_parameters_init(&unbox_params);
    unbox_params.format = boxing_format.config; // må settes
    //unbox_params.pre_filter.coeff = &filterCoeffs2DAllPass;
    unbox_params.pre_filter.process = NULL;
    unbox_params.is_raw = 0;

    typedef unsigned char boxing_image8_pixel;

    typedef struct boxing_image8_s
    {
        unsigned int width;
        unsigned int height;
        DBOOL is_owning_data;
        boxing_image8_pixel * data; //  1D array. Pixel @ (x,y) = data[y*width+x]
    } boxing_image8;

    gvector* output_data = gvector_create(1, 0);
    boxing_metadata_list *metadata = boxing_metadata_list_create();
    boxing_image8* input_image = boxing_image8_create(width, height);
    int process_result = boxing_unboxer_unbox(output_data, metadata, input_image, unboxer, &extract_result, output_data);

    boxing_metadata_list_free(metadata);

    (*env)->ReleaseByteArrayElements(env, image_, image, 0);
}
/*
    boxing_float filterCoeffAllPass[1][1] = { { 1.0000000 }, };
    // setup the unboxer
    boxing_unboxer_parameters unbox_params;
    boxing_unboxer_parameters_init(&unbox_params);
    unbox_params.format = boxing_format.config; // må settes
    unbox_params.pre_filter.coeff = &filterCoeffs2DAllPass;
    unbox_params.pre_filter.process = NULL;
    unbox_params.is_raw = 0;

typedef unsigned char boxing_image8_pixel;

typedef struct boxing_image8_s
{
    unsigned int width;
    unsigned int height;
    DBOOL is_owning_data;
    boxing_image8_pixel * data; //  1D array. Pixel @ (x,y) = data[y*width+x]
} boxing_image8;
 * */