/*****************************************************************************
**
**  Implementation of the genericframefactory interface
**
**  Creation date:  2014/12/16
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include    "boxing/graphics/genericframefactory.h"
#include    "boxing/graphics/genericframegpf_b1.h"
#include    "boxing/graphics/genericframegpf_1.h"
#include    "boxing/platform/memory.h"
#include    "config.h"
#include    "boxing/log.h"

//  PRIVATE INTERFACE
//

static boxing_frame * generic_frame_gpf_b1(const boxing_config * config);
static boxing_frame * generic_frame_gpf_1(const boxing_config * config);

// PUBLIC GENERIC FRAME FACTORY FUNCTIONS
//

struct boxing_frame_s * boxing_generic_frame_factory_create(const boxing_config * config)
{
    if(!config)
    {
        DLOG_ERROR( "config is NULL" );
        return NULL;
    }

    /* old school frame format */
    if (!boxing_config_is_set(config, "FrameFormat", "type"))
    {
        return generic_frame_gpf_b1(config);
    }
    else
    {
        const g_variant * type = boxing_config_property_const(config, "FrameFormat", "type");
        if (type == NULL)
        {
            DLOG_ERROR( "Key 'type' is missing from config" );
            return NULL;
        }
        char * typestr = g_variant_to_string(type);
        if(boxing_string_equal("GPFv1.0", typestr))
        {
            boxing_memory_free(typestr);
            return generic_frame_gpf_1(config);
        }
        else if (boxing_string_equal("GPFv1.1", typestr))
        {
            boxing_memory_free(typestr);
            return generic_frame_gpf_1(config);
        }
        else if (boxing_string_equal("GPFv1.2", typestr))
        {
            boxing_memory_free(typestr);
            return generic_frame_gpf_1(config);
        }
        else
        {
            DLOG_ERROR( "Illegal frame type" );
            boxing_memory_free(typestr);
            return NULL;
        }
    }
}

void boxing_generic_frame_factory_free(struct boxing_frame_s * frame)
{
    if (frame)
    {
        if (frame->free)
        {
            frame->free(frame);
        }
        boxing_memory_free(frame);
    }
}


// PRIVATE GENERIC FRAME FACTORY FUNCTIONS
//

static int get_parameter_int(const boxing_config * config, const char * class_name, const char * parameter, int default_value)
{
    return boxing_config_is_set(config, class_name, parameter) ?
            g_variant_to_int(boxing_config_property_const(config, class_name, parameter)) : default_value;
}

static const char * get_parameter_string(const boxing_config * config, const char * class_name, const char * parameter, const char * default_value)
{
    if (!boxing_config_is_set(config, class_name, parameter))
    {
        return default_value;
    }
    else
    {
        return g_variant_if_string(boxing_config_property_const(config, class_name, parameter));
    }
}

static boxing_frame * generic_frame_gpf_1(const boxing_config * config)
{
    if(!(boxing_config_is_set(config, "FrameFormat", "width") && 
        boxing_config_is_set(config, "FrameFormat", "height")))
    {
        return 0;
    }

    
    const int width                       = g_variant_to_int(boxing_config_property_const(config, "FrameFormat", "width"));
    const int height                      = g_variant_to_int(boxing_config_property_const(config, "FrameFormat", "height"));
    const int border                      = get_parameter_int(config, "FrameFormat", "border",                    1);
    const int border_gap                  = get_parameter_int(config, "FrameFormat", "borderGap",                 1);
    const int corner_mark_size            = get_parameter_int(config, "FrameFormat", "cornerMarkSize",           32);
    const int corner_mark_gap             = get_parameter_int(config, "FrameFormat", "cornerMarkGap",             1);
    const int tiles_per_column            = get_parameter_int(config, "FrameFormat", "tilesPerColumn",            4);
    const int reference_bar_freq_divider  = get_parameter_int(config, "FrameFormat", "referenceBarFreqDivider",   1);
    const int analog_content_symbol_size  = get_parameter_int(config, "FrameFormat", "analogContentSymbolSize",   1);
    const int digital_content_symbol_size = get_parameter_int(config, "FrameFormat", "digitalContentSymbolSize",  1);
          int reference_bar_sync_distance = get_parameter_int(config, "FrameFormat", "refBarSyncDistance",       -1);
          int reference_bar_sync_offset   = get_parameter_int(config, "FrameFormat", "refBarSyncOffset",          0);
    const char * name                     = get_parameter_string(config, "FormatInfo",  "name",                     "");
    const char * short_description        = get_parameter_string(config, "FormatInfo",  "shortDescription",         "");
    const char * description              = get_parameter_string(config, "FormatInfo",  "description",              "");

    const char * frame_format_version = get_parameter_string(config, "FrameFormat", "type", "GPFv1.0");
    if (boxing_string_equal("GPFv1.0", frame_format_version))
    {
        reference_bar_sync_distance = -1;
        reference_bar_sync_offset = 0;
    }

    if(!boxing_config_is_set(config, "FrameFormat", "maxLevelsPerSymbol"))
    {
        return 0;
    }
    const int max_levels_per_symbol = g_variant_to_int(boxing_config_property_const(config, "FrameFormat", "maxLevelsPerSymbol"));

    boxing_frame * frame = (boxing_frame*)BOXING_MEMORY_ALLOCATE_TYPE(boxing_frame_gpf_1);
    boxing_generic_frame_gpf_1_init((boxing_frame_gpf_1*)frame, width, height, border, border_gap, corner_mark_size,
                                                             corner_mark_gap, tiles_per_column, max_levels_per_symbol, reference_bar_freq_divider,
                                                             analog_content_symbol_size, digital_content_symbol_size,
                                                             reference_bar_sync_distance, reference_bar_sync_offset);

    boxing_generic_frame_set_format(frame, frame_format_version);
    boxing_generic_frame_set_name(frame, name);
    boxing_generic_frame_set_short_description(frame, short_description);
    boxing_generic_frame_set_description(frame, description);

    /* set tracker spesific options */
    if (boxing_config_is_set(config, "CodecDispatcher", "DataCodingScheme")) 
    {
        gvector * list_process = boxing_config_parse_list_properties((boxing_config *)config, "CodecDispatcher", "DataCodingScheme");

        if ((boxing_string_equal("GPFv1.0", frame_format_version) == DTRUE) || (boxing_string_equal("GPFv1.1", frame_format_version) == DTRUE))
        {
            for (unsigned int i = 0; i < list_process->size; i++)
            {
                char * class_name_str = GVECTORN(list_process, char *, i);
                if (boxing_string_equal(class_name_str, "SyncPointInserter") && boxing_config_is_set(config, class_name_str, "codec"))
                {
                    const int sync_point_distance_pixel = get_parameter_int(config, "SyncPointInserter", "SyncPointDistancePixel", 100);
                    const int sync_point_radius_pixel = get_parameter_int(config, "SyncPointInserter", "SyncPointRadiusPixel", 2);

                    const int sync_point_h_distance_pixel = get_parameter_int(config, "SyncPointInserter", "SyncPointHDistancePixel", sync_point_distance_pixel);
                    const int sync_point_v_distance_pixel = get_parameter_int(config, "SyncPointInserter", "SyncPointVDistancePixel", sync_point_distance_pixel);
                    const int sync_point_h_offset_pixel = get_parameter_int(config, "SyncPointInserter", "SyncPointVOffsetPixel", -1);
                    const int sync_point_v_offset_pixel = get_parameter_int(config, "SyncPointInserter", "SyncPointHOffsetPixel", -1);

                    boxing_generic_frame_gpf_1_set_tracker_options((boxing_frame_gpf_1*)frame, sync_point_h_distance_pixel, sync_point_v_distance_pixel,
                        sync_point_h_offset_pixel, sync_point_v_offset_pixel, sync_point_radius_pixel);
                }
            }
        }
        else
        {
            const int sync_point_radius_pixel = get_parameter_int(config, "FrameFormat", "syncPointRadius", 3);
            const int sync_point_h_distance_pixel = get_parameter_int(config, "FrameFormat", "syncPointHDistance", 100);
            const int sync_point_v_distance_pixel = get_parameter_int(config, "FrameFormat", "syncPointVDistance", 100);
            const int sync_point_h_offset_pixel = get_parameter_int(config, "FrameFormat", "syncPointVOffset", -1);
            const int sync_point_v_offset_pixel = get_parameter_int(config, "FrameFormat", "syncPointHOffset", -1);

            boxing_generic_frame_gpf_1_set_tracker_options((boxing_frame_gpf_1*)frame, sync_point_h_distance_pixel, sync_point_v_distance_pixel,
                sync_point_h_offset_pixel, sync_point_v_offset_pixel, sync_point_radius_pixel);

        }
        gvector_free(list_process);
    }

    return frame;
}

static boxing_frame * generic_frame_gpf_b1(const boxing_config * config)
{
    DBOOL has_point = DFALSE;
    boxing_pointi size = boxing_config_property_pointi((boxing_config *)config, "FrameBuilder", "dimensions", &has_point);

    if (!has_point)
    {
        return NULL;
    }

    boxing_frame * frame = (boxing_frame *)BOXING_MEMORY_ALLOCATE_TYPE(boxing_frame_gpf_b1);
    boxing_frame_gpf_b1_init((boxing_frame_gpf_b1*)frame, size.x, size.y, 1, 0, 32, 1, 4, 2);

    boxing_generic_frame_set_name(frame, "alpha");
    boxing_generic_frame_set_short_description(frame, "");
    boxing_generic_frame_set_description(frame, "");

    return frame;
}
