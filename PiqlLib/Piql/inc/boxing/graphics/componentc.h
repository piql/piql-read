#ifndef DCOMPONENTC_H
#define DCOMPONENTC_H

/*****************************************************************************
**
**  Definition of the component class
**
**  Creation date:  2014/12/16
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/graphics/painter.h"

//============================================================================
//  CLASS:  boxing_component

typedef struct boxing_component_s
{

    void  (*add)(struct boxing_component_s * component, struct boxing_component_s * child);
    void  (*render)(struct boxing_component_s * component, boxing_painter * painter);
    void  (*free)(struct boxing_component_s * component); // destructor

    gvector                     children; // of struct boxing_component_*
    boxing_pointi               pos;
    boxing_pointi               size;
    int                         background_color;
    int                         foreground_color;
    struct boxing_component_s * parent;
    struct boxing_component_s * peer;
} boxing_component;

void          boxing_component_init(boxing_component *component, boxing_component * parent); // parent is null by default
boxing_pointi absoluteLocation(const boxing_component *component);
void          boxing_component_free(boxing_component *component); // does not free component pointer itself
int           get_background_color(const struct boxing_component_s * component);
int           get_foreground_color(const struct boxing_component_s * component);

//============================================================================


#endif // DCOMPONENTC_H
