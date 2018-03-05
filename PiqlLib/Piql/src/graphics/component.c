/*****************************************************************************
**
**  Implementation of the component interface
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
#include    "boxing/graphics/component.h"
#include    "boxing/platform/memory.h"


// PUBLIC COMPONENT FUNCTIONS
//

//----------------------------------------------------------------------------
/*!
 *  Parent is null by default.
 */
void boxing_component_init(boxing_component *component, boxing_component * parent)
{
    component->parent = parent;
    component->peer = NULL;
    component->background_color = 0;
    component->foreground_color = 1;
    component->size.x = component->size.y = 0;
    component->pos.x = component->pos.y = 0;
    component->add = boxing_component_add;
    component->render = boxing_component_render;
    component->free = boxing_component_free;
    component->set_size = boxing_component_set_size;

    gvector_create_inplace(&component->children, sizeof(boxing_component *), 0);
}

boxing_pointi boxing_component_absolute_location(const boxing_component *component)
{
    boxing_pointi return_value;

    if(component->peer)
    {
        return_value = boxing_component_absolute_location(component->peer);
        return_value.x += component->pos.x;
        return_value.y += component->pos.y;
        return return_value;
    }
    else
    {
        return component->pos;
    }
}

//----------------------------------------------------------------------------
/*!
 *  Does not free component pointer itself.
 */
void boxing_component_free(boxing_component *component)
{
    boxing_memory_free(component->children.buffer);
}

void boxing_component_add(struct boxing_component_s *component, struct boxing_component_s * child)
{
    child->peer = component;
    gvector_append_data(&component->children, 1, &child);
}

void boxing_component_render(struct boxing_component_s *component, boxing_painter * painter)
{
    boxing_component ** it;
    boxing_component ** it_end = (((boxing_component**)(component->children.buffer)) + component->children.size);
    for(it = component->children.buffer; it != it_end; it++)
    {
        boxing_component * child = *it;
        boxing_painter child_painter;
        boxing_recti recti = {child->pos.x, child->pos.y, child->size.x, child->size.y};
        boxing_painter_clip(&child_painter, painter, &recti);
        child->render(child, &child_painter);
    }
}

void boxing_component_set_size(struct boxing_component_s *component, int width, int height)
{
    component->size.x = width;
    component->size.y = height;
}

int boxing_component_get_background_color(const struct boxing_component_s *component)
{
    while (component->parent)
    {
        component = component->parent;
    }
    return component->background_color;
}

int boxing_component_get_foreground_color(const struct boxing_component_s *component)
{
    while (component->parent)
    {
        component = component->parent;
    }
    return component->foreground_color;
}

