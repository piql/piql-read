//
// Created by hcon on 14.03.18.
//Used to interface with java.
//
#include <stdlib.h>
#include <jni.h>
#include <Piql/inc/boxing/metadata.h>
#include <Piql/inc/boxing/unboxer.h>
#include <Piql/inc/boxing/platform/memory.h>
#include <Piql/inc/boxing/unboxingutility.h>
#include <android/log.h>
#include <Piql/thirdparty/glib/gvector.h>
#include "string.h"
#include "boxing/config.h"
#include "boxing_config.h"


#define LOGGING_ENABLED
#define LOG_TAG "Wrapper"

void boxing_log( int log_level, const char * string );
void boxing_log_args( int log_level, const char * format, ... );

static const char * result_names[] = { "OK",
                                       "METADATA ERROR",
                                       "BORDER TRACKING ERROR",
                                       "DATA DECODE ERROR",
                                       "CRC MISMATCH ERROR",
                                       "CONFIG ERROR",
                                       "PROCESS CALLBACK ABORT" };

static const char * content_types[] = { "UNKNOWN",
                                        "TOC",
                                        "DATA",
                                        "VISUAL",
                                        "CONTROL FRAME" };


static int unboxing_complete_callback(void * user, int* res, boxing_stats_decode * stats)
{
    BOXING_UNUSED_PARAMETER(user);

    if (*res == BOXING_UNBOXER_OK)
    {
        boxing_log_args(1,"Unboxing success!\n");

        boxing_log_args(1,"FEC Accumulated amount: %f\n", stats->fec_accumulated_amount);
        boxing_log_args(1,"FEC Accumulated weight: %f\n", stats->fec_accumulated_weight);
        boxing_log_args(1,"Resolved errors: %i\n", stats->resolved_errors);
        boxing_log_args(1,"Unresolved errors: %i\n\n", stats->unresolved_errors);
    }
    else
    {
        boxing_log(1,"Failed wrapper:unboxing_complete_callback");
    }

    return 0;
}

//----------------------------------------------------------------------------
/*! \ingroup unboxtests
 *
 *  Unboxing metadata complete callback function.
 *
 *  \param user       User data
 *  \param res        Unboxing result.
 *  \param meta_data  Unboxing file meta data
 */

static int metadata_complete_callback(void * user, int* res, boxing_metadata_list * meta_data)
{
    BOXING_UNUSED_PARAMETER(user);

    if (*res == BOXING_UNBOXER_OK)
    {
        printf("\nMeta data to current file:\n");

        GHashTableIter iter;
        gpointer key, value;

        g_hash_table_iter_init(&iter, meta_data);

        while (g_hash_table_iter_next(&iter, &key, &value))
        {
            boxing_metadata_item * item = (boxing_metadata_item *)value;

            boxing_metadata_type type = item->base.type;
            switch (type)
            {
                case BOXING_METADATA_TYPE_JOBID:
                    boxing_log_args(1,"Job id: %d\n", ((boxing_metadata_item_job_id*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_FRAMENUMBER:
                    boxing_log_args(1,"Frame number: %d\n", ((boxing_metadata_item_frame_number*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_FILEID:
                    boxing_log_args(1,"File id: %d\n", ((boxing_metadata_item_file_id*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_FILESIZE:
                    boxing_log_args(1,"File size: %llu\n", ((boxing_metadata_item_file_size*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_DATACRC:
                    boxing_log_args(1,"Data CRC: %llu\n", ((boxing_metadata_item_data_crc*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_DATASIZE:
                    boxing_log_args(1,"Data size: %d\n", ((boxing_metadata_item_data_size*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_SYMBOLSPERPIXEL:
                    boxing_log_args(1,"Symbols per pixel: %d\n", ((boxing_metadata_item_symbols_per_pixel*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_CONTENTTYPE:
                    boxing_log_args(1,"Content type: %s\n", content_types[((boxing_metadata_item_content_type*)item)->value]);
                    break;
                case BOXING_METADATA_TYPE_CIPHERKEY:
                    boxing_log_args(1,"Cipher key: %d\n", ((boxing_metadata_item_cipher_key*)item)->value);
                    break;
                case BOXING_METADATA_TYPE_CONTENTSYMBOLSIZE:
                    boxing_log_args(1,"Content symbol size: %d\n", ((boxing_metadata_item_content_symbol_size*)item)->value);
                    break;
                default:
                    break;
            }
        }
    }
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_no_ntnu_bachelor2018_filmreader_PiqlLib_Wrapper_process(JNIEnv *env, jclass type, jint width,
                                                             jint height, jbyteArray image_) {
    jbyte *image = (*env)->GetByteArrayElements(env, image_, NULL);

    gvector* output_data = gvector_create(1, 0);
    boxing_image8* input_image = boxing_image8_create((unsigned int)width, (unsigned int)height);

    boxing_memory_copy(input_image ->data,image,(unsigned int)width*height);


    boxing_unboxer_utility * util =  boxing_unboxer_utility_create("4k-stud-PAM2-270x270",DFALSE,unboxing_complete_callback,metadata_complete_callback);
    //boxing_unboxer_utility * util =  boxing_unboxer_utility_create("4k-stud-PAM2-360x360",DFALSE,unboxing_complete_callback,metadata_complete_callback);
    //boxing_unboxer_utility * util =  boxing_unboxer_utility_create("4k-stud-PAM2-540x540",DFALSE,unboxing_complete_callback,metadata_complete_callback);

    int process_result = boxing_unboxer_utility_unbox(util, input_image, output_data);
    if(process_result == BOXING_UNBOXER_OK){
        FILE * out_file = fopen("/data/data/filmreader.bacheloroppg.ntnu.no.filmreader/app_tardir/output.tar","w");
        if(out_file != NULL){
            fwrite(output_data->buffer,output_data ->item_size, output_data ->size,out_file);
            fclose(out_file);

        }

    }
    gvector_free(output_data);
    boxing_image8_free(input_image);
    boxing_unboxer_utility_free(util);

    (*env)->ReleaseByteArrayElements(env, image_, image, 0);
    return (process_result == BOXING_UNBOXER_OK);
}

#if defined (LOGGING_ENABLED)

void boxing_log(int log_level, const char *string) {
    //printf( "%d : %s\n", log_level, string );
    __android_log_print(ANDROID_LOG_INFO,LOG_TAG,"%d : %s\n",log_level,string);
}

void boxing_log_args(int log_level, const char *format, ...) {
    va_list args;
    va_start(args, format);

    __android_log_vprint(ANDROID_LOG_INFO, LOG_TAG, format, args);

    va_end(args);
}

#else
void boxing_log(int log_level, const char * string) {}
void boxing_log_args(int log_level, const char * format, ...) {}
#endif // LOGGING_ENABLED

void(*boxing_log_custom)(int log_level, const char * string) = NULL;
void(*boxing_log_args_custom)(int log_level, const char * format, va_list args) = NULL;

