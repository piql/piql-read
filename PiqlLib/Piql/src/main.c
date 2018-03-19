/*****************************************************************************
**
**  Implementation of the unboxer test application
**
**  Creation date:  2016/06/28
**  Created by:     Ole Liabo
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/unboxingutility.h"
#include <stdarg.h>
#include <stdio.h>
#include <jni.h>


#if defined (D_OS_WIN32)
#include <io.h>
#else
#include <unistd.h>
#endif

//  DEFINES
//
#define LOGGING_ENABLED // Enables log output from the unboxing library

#if defined ( D_OS_WIN32 )
#define DFSEEK _fseeki64
#define DFTELL _ftelli64
#else
#define DFSEEK fseeko
#define DFTELL ftello
#endif


typedef struct command_line_parameters_s
{
    unsigned int input_start_index;
    unsigned int input_end_index;
    size_t       x_size;
    size_t       y_size;
    const char*  format;
    const char*  output;
    DBOOL        is_raw;
}command_line_parameters;

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


//---------------------------------------------------------------------------- 
/*! \ingroup unboxtests
 *
 *  Getting the string representation of enum.
 *
 *  \param result  Result enum.
 */

static const char * get_process_result_name(enum boxing_unboxer_result result)
{
    return result_names[result];
}


//---------------------------------------------------------------------------- 
/*! \ingroup unboxtests
 *
 *  Unboxing complete callback function. 
 *
 *  \param user   User data
 *  \param res    Unboxing result.
 *  \param stats  Unboxing statistics 
 */

static int unboxing_complete_callback(void * user, int* res, boxing_stats_decode * stats)
{
    BOXING_UNUSED_PARAMETER(user);

    if (*res == BOXING_UNBOXER_OK)
    {
        printf("Unboxing success!\n");

        printf("FEC Accumulated amount: %f\n", stats->fec_accumulated_amount);
        printf("FEC Accumulated weight: %f\n", stats->fec_accumulated_weight);
        printf("Resolved errors: %i\n", stats->resolved_errors);
        printf("Unresolved errors: %i\n\n", stats->unresolved_errors);
    }
    else
    {
        fprintf(stderr, "Unboxing failed! Errorcode = %i - %s\n", *res, get_process_result_name(*res));
    }

    return 0;
}


//---------------------------------------------------------------------------- 
/*! \ingroup unboxtests
 *
 *  Save output data to the file. 
 *
 *  \param output_data       Output data.
 *  \param output_file_name  Output file name.
 */

static int save_output_data(gvector* output_data, const char * output_file_name)
{
    // Save output data to the file
#ifndef WIN32
    FILE * out_file = fopen(output_file_name, "a+");
#else
    FILE * out_file = fopen(output_file_name, "a+b");
#endif
    if (out_file == NULL)
    {
        fprintf(stderr, "Failed to create output file %s\n", output_file_name);
        return -1;
    }

    if (output_data->size != fwrite(output_data->buffer, output_data->item_size, output_data->size, out_file))
    {
        fclose(out_file);
        fprintf(stderr, "Output file write error.\n");
        return -1;
    }
    else
    {
        printf("Output data saved to the file %s\n\n", output_file_name);
    }
    fclose(out_file);

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
                printf("Job id: %d\n", ((boxing_metadata_item_job_id*)item)->value);
                break;
            case BOXING_METADATA_TYPE_FRAMENUMBER:
                printf("Frame number: %d\n", ((boxing_metadata_item_frame_number*)item)->value);
                break;
            case BOXING_METADATA_TYPE_FILEID:
                printf("File id: %d\n", ((boxing_metadata_item_file_id*)item)->value);
                break;
            case BOXING_METADATA_TYPE_FILESIZE:
                printf("File size: %llu\n", ((boxing_metadata_item_file_size*)item)->value);
                break;
            case BOXING_METADATA_TYPE_DATACRC:
                printf("Data CRC: %llu\n", ((boxing_metadata_item_data_crc*)item)->value);
                break;
            case BOXING_METADATA_TYPE_DATASIZE:
                printf("Data size: %d\n", ((boxing_metadata_item_data_size*)item)->value);
                break;
            case BOXING_METADATA_TYPE_SYMBOLSPERPIXEL:
                printf("Symbols per pixel: %d\n", ((boxing_metadata_item_symbols_per_pixel*)item)->value);
                break;
            case BOXING_METADATA_TYPE_CONTENTTYPE:
                printf("Content type: %s\n", content_types[((boxing_metadata_item_content_type*)item)->value]);
                break;
            case BOXING_METADATA_TYPE_CIPHERKEY:
                printf("Cipher key: %d\n", ((boxing_metadata_item_cipher_key*)item)->value);
                break;
            case BOXING_METADATA_TYPE_CONTENTSYMBOLSIZE:
                printf("Content symbol size: %d\n", ((boxing_metadata_item_content_symbol_size*)item)->value);
                break;
            default:
                break;
            }
        }
    }
    printf("\n");
    return 0;
}


//---------------------------------------------------------------------------- 
/*! \ingroup unboxtests
 *
 *  Read unboxing parameters from input command line. If return_parameters.input_start_index not equal to zero, then reading parameters is successful.
 *
 *  \param[in] argc   Number parameters in the input command line.
 *  \param[in] argv   Array parameters in the input command line.
 *  \return unboxing parameters from input command line.
 */

static command_line_parameters get_command_line_parameters(int argc, char *argv[])
{
    command_line_parameters return_parameters;

    return_parameters.input_start_index = 0;
    return_parameters.input_end_index = 0;
    return_parameters.x_size = 0;
    return_parameters.y_size = 0;
    return_parameters.format = NULL;
    return_parameters.output = NULL;
    return_parameters.is_raw = DFALSE;

    int arg_index = 1;
    while (arg_index < argc)
    {
        if (boxing_string_equal(argv[arg_index], "-i") == DTRUE)
        {
            while (++arg_index < argc && argv[arg_index][0] != '-')
            {
                if (return_parameters.input_start_index == 0)
                {
                    return_parameters.input_start_index = arg_index;
                }
            }
            if (return_parameters.input_start_index != 0)
            {
                return_parameters.input_end_index = arg_index - 1;
            }
        }
        else if (boxing_string_equal(argv[arg_index], "-s") == DTRUE)
        {
            if (argc <= arg_index + 2)
            {
                break;
            }
            if (argv[arg_index + 1][0] == '-' || argv[arg_index + 2][0] == '-')
            {
                break;
            }
            return_parameters.x_size = atoi(argv[++arg_index]);
            return_parameters.y_size = atoi(argv[++arg_index]);
            arg_index++;
        }
        else if (boxing_string_equal(argv[arg_index], "-f") == DTRUE)
        {
            if (argc <= arg_index + 1)
            {
                break;
            }
            if (argv[arg_index + 1][0] == '-')
            {
                break;
            }
            return_parameters.format = argv[++arg_index];
            arg_index++;
        }
        else if (boxing_string_equal(argv[arg_index], "-o") == DTRUE)
        {
            if (argc <= arg_index + 1)
            {
                break;
            }
            if (argv[arg_index + 1][0] == '-')
            {
                break;
            }

            return_parameters.output = argv[++arg_index];
            arg_index++;
        }
        else if (boxing_string_equal(argv[arg_index++], "-is_raw") == DTRUE)
        {
            return_parameters.is_raw = DTRUE;
        }
        else
        {
            fprintf(stderr, "Unsupported input parameter in the command line!!! (%s)\n", argv[arg_index++]);
        }
    }

    if (return_parameters.input_end_index == 0 || return_parameters.x_size == 0 || return_parameters.y_size == 0 || return_parameters.format == NULL)
    {
        return_parameters.input_start_index = 0;
    }

    if (return_parameters.input_start_index == 0)
    {
        printf(
            "Unboxer sample application - decodes data in file.\n"
            "\n"
            "app -i <input file> <input file> ... -s <width> <height> -f <4kv6|4kv7|4kv8|4kv9|4kv10> [output-file] -is_raw\n"
            "\n"
            "Where:\n"
            "   -i            : Key to specify the input files.\n"
            "   input file    : Name of the binary input file in the following format: \n"
            "                   width * height bytes: 8 bit per pixel grayscale data.\n"
            "   -s            : Key to specify the sizes.\n"
            "   width         : Width of image in bytes.\n"
            "   height        : Height of image in bytes.\n"
            "   -f            : Key to specify the boxing format.\n"
            "   boxing-format : Boxing format name to use when decoding image. Supported formats: 4kv6, 4kv7, 4kv8, 4kv9 and 4kv10.\n"
            "   -o            : (Optional) Key to specify the output file name.\n"
            "   output        : (Optional) Name of the file where decoded output data\n"
            "                   is written.\n"
            "   -is_raw       : Key to specify the RAW format of the input files.\n"
            );
    }

    return return_parameters;
}


static DBOOL check_for_template(char* input_file_name)
{
    for (unsigned int i = 0; i < boxing_string_length(input_file_name); i++)
    {
        if (input_file_name[i] == '%' || input_file_name[i] == '*')
        {
            return DTRUE;
        }
    }

    return DFALSE;
}


static int unbox_current_file(const char* input_file_name, command_line_parameters input_parameters, boxing_unboxer_utility* utility)
{
    const size_t width = input_parameters.x_size;
    const size_t height = input_parameters.y_size;

#ifndef WIN32
    FILE * file = fopen(input_file_name, "r");
#else
    FILE * file = fopen(input_file_name, "rb");
#endif
    if (file == NULL)
    {
        fprintf(stderr, "Failed to read file '%s'.\n", input_file_name);
        return -1;
    }

    // Obtain file size
    if (DFSEEK(file, 0L, SEEK_END) != 0)
    {
        return -1;
    }

    const boxing_int64 file_size = DFTELL(file);
    if(file_size < 0)
    {
        return -1;
    }

    // Go to the starting data position
    if (DFSEEK(file, 0L, SEEK_SET) != 0)
    {
        return -1;
    }

    // We can only match file size with input image size to validate input file
    // Also for now input file has 8 bpp, so it is no need to multiply image size and colour depth.
    if (file_size != (boxing_int64)(width * height))
    {
        printf("File '%s' size (%lld) does not match with input image size (width=%lu height=%lu).\n", input_file_name, file_size, width, height);
        return -1;
    }

    printf("Reading image data file %s ...\n", input_file_name);

    boxing_image8* input_image = boxing_image8_create((unsigned int)width, (unsigned int)height);
    if (1 != fread(input_image->data, width * height, 1, file))
    {
        fprintf(stderr, "Failed to read image data.\n");
        return -1;
    }

    printf("The image data file read successfully!\nStarting unboxing...\n");

    fclose(file);

    gvector* output_data = gvector_create(1, 0);
    int process_result = boxing_unboxer_utility_unbox(utility, input_image, output_data);
    if (save_output_data(output_data, input_parameters.output) != 0)
    {
        return -1;
    }

    gvector_free(output_data);
    boxing_image8_free(input_image);

    if (process_result != BOXING_UNBOXER_OK)
    {
        return -1;
    }

    return 0;
}


static void change_symbols(char* input_file_name)
{
    for (unsigned int i = 0; i < boxing_string_length(input_file_name); i++)
    {
        if (input_file_name[i] == '*')
        {
            input_file_name[i] = '%';
        }
    }
}


//---------------------------------------------------------------------------- 
/*! \ingroup unboxtests
 * 
 *  Unboxing sample application.
 *
 */

int main(int argc, char *argv[])
{
    command_line_parameters command_line_parameters = get_command_line_parameters(argc, argv);

    // If the parameters in the command line is invalid, then return
    if (command_line_parameters.input_start_index == 0)
    {
        return -1;
    }

    if (command_line_parameters.output != NULL)
    {
#ifndef WIN32
        FILE * out_file = fopen(command_line_parameters.output, "w+");
#else
        FILE * out_file = fopen(command_line_parameters.output, "w+b");
#endif
        fclose(out_file);
    }

#ifdef BOXINGLIB_CALLBACK
    boxing_unboxer_utility* utility = boxing_unboxer_utility_create(command_line_parameters.format, command_line_parameters.is_raw, unboxing_complete_callback, metadata_complete_callback);
#else
    boxing_unboxer_utility* utility = boxing_unboxer_utility_create(command_line_parameters.format, command_line_parameters.is_raw);
#endif

    while (command_line_parameters.input_start_index <= command_line_parameters.input_end_index)
    {
        char* input_file_name = argv[command_line_parameters.input_start_index++];

        if (check_for_template(input_file_name) == DFALSE)
        {
            printf("Input file name - %s\n", input_file_name);
            unbox_current_file(input_file_name, command_line_parameters, utility);
        }
        else
        {
            int current_file_number = 0;
            DBOOL end_of_cycle = DFALSE;
            change_symbols(input_file_name);

            printf("Input file name template - %s\n", input_file_name);
            
            while (end_of_cycle == DFALSE)
            {
                char* current_input_file_name = (char *)malloc(boxing_string_length(input_file_name) + 8);
                sprintf(current_input_file_name, input_file_name, current_file_number);
                
                if (access(current_input_file_name, 0) == -1)
                {
                    end_of_cycle = DTRUE;
                    if (current_file_number == 0)
                    {
                        fprintf(stderr, "Filed to read files from input template!\n");
                    }

                    boxing_string_free(current_input_file_name);

                    continue;
                }

                int result = unbox_current_file(current_input_file_name, command_line_parameters, utility);

                if (result != BOXING_UNBOXER_OK)
                {
                    boxing_string_free(current_input_file_name);

                    break;
                }

                current_file_number++;

                boxing_string_free(current_input_file_name);
            }
        }
    }

    boxing_unboxer_utility_free(utility);
    
    return 0;
}

#if defined (LOGGING_ENABLED)
void boxing_log( int log_level, const char * string )
{
    printf( "%d : %s\n", log_level, string );
}

void boxing_log_args( int log_level, const char * format, ... )
{
    va_list args;
    va_start(args, format);

    printf( "%d : ", log_level );
    vprintf( format, args );
    printf( "\n" );

    va_end(args);
}
#else
void boxing_log(int log_level, const char * string) {}
void boxing_log_args(int log_level, const char * format, ...) {}
#endif // LOGGING_ENABLED

void(*boxing_log_custom)(int log_level, const char * string) = NULL;
void(*boxing_log_args_custom)(int log_level, const char * format, va_list args) = NULL;
