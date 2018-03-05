/*****************************************************************************
**
**  Implementation of the referencepoint class
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
#include    "boxing/graphics/referencepoint.h"

//  DEFINES
//

#define SMEMBER(member) (((boxing_reference_point*)point)->member)

//  PRIVATE INTERFACE
//

static void render(boxing_component * point, boxing_painter * painter);

// PUBLIC REFERENCE POINT FUNCTIONS
//

void boxing_reference_point_init(boxing_reference_point * point, int gap_size)
{
    boxing_component_init((boxing_component*)point, NULL);
    point->base.render = render;
    point->gap_size = gap_size;
}

// PRIVATE REFERENCE POINT FUNCTIONS
//

static void render(boxing_component * point, boxing_painter * painter)
{
    int tile_size = ((point->size.x > point->size.y) ? point->size.x: point->size.y - (SMEMBER(gap_size) * 2)) / 2;
    painter->fill_rect(painter, SMEMBER(gap_size) + tile_size, SMEMBER(gap_size), tile_size, tile_size, boxing_component_get_foreground_color(point));
    painter->fill_rect(painter, SMEMBER(gap_size), SMEMBER(gap_size) + tile_size, tile_size, tile_size, boxing_component_get_foreground_color(point));
    boxing_component_render(point, painter);
}
