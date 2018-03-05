/*****************************************************************************
**
**  Implementation of the generic frame interface
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
#include <string.h>
#include <Piql/inc/boxing/platform/platform.h>
#include "boxing/graphics/genericframe.h"
#include "boxing/utils.h"
#include "boxing/string.h"
#include "boxing/platform/memory.h"

//  PRIVATE INTERFACE
//

static boxing_pointi get_tile_size(struct boxing_generic_container_s * container);
static boxing_pointi get_tile_size_enum(struct boxing_generic_container_s * container, enum boxing_content_type type);

// PUBLIC GENERIC FRAME FUNCTIONS
//

void boxing_generic_frame_container_set_tile_size(boxing_generic_container * container, const boxing_pointi size)
{
    container->tile_size = size;
}

void boxing_generic_frame_container_init(boxing_generic_container * container)
{
    container->tile_size.x = container->tile_size.y = 1;
    container->set_tile_size = boxing_generic_frame_container_set_tile_size;
    container->get_tile_size_enum = get_tile_size_enum;
    container->get_tile_size = get_tile_size;
    container->capasity = NULL;
    container->dimension = NULL;
    container->location = NULL;
    container->set_data = NULL;
}

// generic frame stuff


const char * boxing_generic_frame_get_format(const boxing_frame * frame)
{
    return frame->format;
}

const char * boxing_generic_frame_get_name(const struct boxing_frame_s * frame)
{
    return frame->name;
}

const char * boxing_generic_frame_get_short_description(const struct boxing_frame_s * frame)
{
    return frame->short_description;
}

const char * boxing_generic_frame_get_description(const struct boxing_frame_s * frame)
{
    return frame->description;
}

void  boxing_generic_frame_set_format(boxing_frame * frame, const char * format)
{
    boxing_memory_free(frame->format);
    frame->format = boxing_string_clone(format);
}

void boxing_generic_frame_set_name(struct boxing_frame_s * frame, const char * name)
{
    boxing_memory_free(frame->name);
    frame->name = boxing_string_clone(name);
}

void boxing_generic_frame_set_short_description(struct boxing_frame_s * frame, const char * name)
{
    boxing_memory_free(frame->short_description);
    frame->short_description = boxing_string_clone(name);
}

void boxing_generic_frame_set_description(struct boxing_frame_s * frame, const char * name)
{
    boxing_memory_free(frame->description);
    frame->description = boxing_string_clone(name);
}

void boxing_generic_frame_free(struct boxing_frame_s * frame)
{    
    boxing_memory_free(frame->format);
    boxing_memory_free(frame->name);
    boxing_memory_free(frame->short_description);
    boxing_memory_free(frame->description);
}

void boxing_generic_frame_init(struct boxing_frame_s * frame)
{
    memset(frame, 0, sizeof(boxing_frame));
    frame->free = boxing_generic_frame_free;
}


// PRIVATE GENERIC FRAME FUNCTIONS
//

static boxing_pointi get_tile_size(struct boxing_generic_container_s * container)
{
    return container->tile_size;
}

static boxing_pointi get_tile_size_enum(struct boxing_generic_container_s * container, enum boxing_content_type type)
{
    BOXING_UNUSED_PARAMETER(type);
    return container->tile_size;
}
