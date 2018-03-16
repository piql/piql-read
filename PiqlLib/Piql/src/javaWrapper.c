//
// Created by hcon on 14.03.18.
//Used to interface with java.
//
/*
#include <jni.h>
#include <Piql/inc/boxing/metadata.h>
#include <Piql/inc/boxing/unboxer.h>
#include <Piql/inc/boxing/platform/memory.h>
#include "string.h"

JNIEXPORT void JNICALL
Java_no_ntnu_bachelor2018_filmreader_PiqlLib_Wrapper_process(JNIEnv *env, jobject instance,
                                                             jint argc, jcharArray argv_) {
    jchar *argv = (*env)->GetCharArrayElements(env, argv_, NULL);
    int extract_result;
    unsigned int height = 0, width = 0;
    //boxing_unboxer_unbox(gvector * data, boxing_metadata_list * metadata, boxing_image8 * image, boxing_unboxer * unboxer, int * extract_result, void *user_data)

    boxing_unboxer_parameters * parameters = BOXING_MEMORY_ALLOCATE_TYPE(boxing_unboxer_parameters);
    boxing_unboxer *unboxer = boxing_unboxer_create(parameters);

    gvector* output_data = gvector_create(1, 0);
    boxing_metadata_list *metadata = boxing_metadata_list_create();
    boxing_image8* input_image = boxing_image8_create(width, height);
    int process_result = boxing_unboxer_unbox(output_data, metadata, input_image, unboxer, &extract_result, output_data);

    boxing_metadata_list_free(metadata);

    (*env)->ReleaseCharArrayElements(env, argv_, argv, 0);
}*/