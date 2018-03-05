/*****************************************************************************
**
**  Implementation of the config interface
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
#include "boxing/config.h"
#include "boxing/globals.h"
#include "boxing/log.h"
#include "boxing/platform/memory.h"
#include "unboxer"
//  SYSTEM INCLUDES
//
#include <stdio.h>

static DBOOL         is_instance_initialized = DFALSE;
static boxing_config instance;


// PUBLIC CONFIG FUNCTIONS
//

//----------------------------------------------------------------------------
/*!
  * An instance. It is not a singleton, so you can have as many configs as you want, but this has global access. No fancy
  * thread-safe instance creation, so just have it called first from not concurrent medium.
  * \return pointer to the global config instance.
 */
boxing_config * boxing_config_instance()
{
    if (!is_instance_initialized)
    {
        is_instance_initialized = DTRUE;
        instance.class_properties =  g_hash_table_new_full(g_str_hash, g_str_equal, boxing_utils_g_hash_table_destroy_item_string, boxing_utils_g_hash_table_destroy_item_ghash);
        instance.class_alias =  g_hash_table_new_full(g_str_hash, g_str_equal, boxing_utils_g_hash_table_destroy_item_string, boxing_utils_g_hash_table_destroy_item_string);
    }
    return &instance;
}


boxing_config * boxing_config_create()
{
    boxing_config * return_value = BOXING_MEMORY_ALLOCATE_TYPE(boxing_config);
    return_value->class_properties =  g_hash_table_new_full(g_str_hash, g_str_equal, boxing_utils_g_hash_table_destroy_item_string, boxing_utils_g_hash_table_destroy_item_ghash);
    return_value->class_alias =  g_hash_table_new_full(g_str_hash, g_str_equal, boxing_utils_g_hash_table_destroy_item_string, boxing_utils_g_hash_table_destroy_item_string);
    return return_value;
}

//----------------------------------------------------------------------------
/**!
  * Frees the allocated config structure.
  */
void boxing_config_free(boxing_config * conf)
{
    if (conf)
    {
        g_hash_table_destroy(conf->class_alias);
        g_hash_table_destroy(conf->class_properties);
        if (is_instance_initialized && (&instance == conf))
        {
            is_instance_initialized = DFALSE;
        }
        else 
        {
            boxing_memory_free(conf);
        }
    }
}

//----------------------------------------------------------------------------
/**!
  * Sets property_value value to name_property property of name_class class as uint
  */
void boxing_config_set_property_uint(boxing_config * config, const char * name_class, const char * name_property, unsigned int property_value)
{// \todo consider optimizing
    char buffer[64];
    sprintf(buffer, "%u", property_value);
    boxing_config_set_property(config, name_class, name_property, buffer);
}

//----------------------------------------------------------------------------
/**!
  * Sets property_value value to name_property property of name_class class as string
  */
void boxing_config_set_property(boxing_config * config, const char * name_class, const char * name_property, const char * property_value)
{
    GHashTable * class_hash;
    GHashTable * class_property;
    const char * alias_name;
    int i;
    if (!boxing_string_equal(name_property, CONFIG_XML_KEY_ALIAS)) 
    {
        class_hash = g_hash_table_lookup(config->class_properties, name_class);
        if (class_hash == NULL)
        {
            GHashTable *map_properties = g_hash_table_new_full(g_str_hash, g_str_equal, boxing_utils_g_hash_table_destroy_item_string, boxing_utils_g_hash_table_destroy_item_g_variant);
            g_hash_table_replace(map_properties, boxing_string_clone(name_property), g_variant_create(property_value));
            g_hash_table_replace(config->class_properties, boxing_string_clone(name_class), map_properties);
        }
        else
        {
            g_hash_table_replace(class_hash, boxing_string_clone(name_property), g_variant_create(property_value));
        }
        alias_name = g_hash_table_lookup(config->class_alias, name_class);
        if(alias_name != NULL) 
        {
            boxing_config_set_property(config, alias_name, name_property, property_value);
        }
    }
    else
    {
        g_hash_table_replace(config->class_alias, boxing_string_clone(property_value), boxing_string_clone(name_class));
        class_property = g_hash_table_lookup(config->class_properties, property_value);
        if (class_property != NULL) {
            // Copy the properties of class property_value to class name_class
            class_hash = g_hash_table_lookup(config->class_properties, name_class);
            if (class_hash == NULL) {
                GHashTable *map_properties = g_hash_table_new_full(g_str_hash, g_str_equal, boxing_utils_g_hash_table_destroy_item_string, boxing_utils_g_hash_table_destroy_item_g_variant);
                g_hash_table_replace(config->class_properties, boxing_string_clone(name_class), map_properties);
                class_hash = map_properties;
            }
            
            for (i = 0; i < class_property->size; i++)
            {
                guint node_hash = class_property->hashes[i];
                gpointer node_key = class_property->keys[i];
                gpointer node_value = class_property->values[i];
                
                if (HASH_IS_REAL (node_hash) )
                {
                    g_hash_table_replace(class_hash, boxing_string_clone((char *)node_key), g_variant_create((char *)node_value));
                }
            }
        }
    }
}

//----------------------------------------------------------------------------
/**!
  * Get name_property property of name_class class
  */
g_variant * boxing_config_property(boxing_config * config, const char * name_class, const char * name_property)
{
    GHashTable * class_hash;
    
    class_hash = g_hash_table_lookup(config->class_properties, name_class);
    if (class_hash != NULL)
    {
        return g_hash_table_lookup(class_hash, name_property);
    }
    return NULL;
}

//----------------------------------------------------------------------------
/**!
  * Get name_property property of name_class class as const g_variant*.
  */
const g_variant * boxing_config_property_const(const boxing_config * config, const char * name_class, const char * name_property)
{
    return boxing_config_property((boxing_config *)config, name_class, name_property);
}

//----------------------------------------------------------------------------
/**!
  * Get name_property property of name_class class as uint
  */
unsigned int boxing_config_property_uint(const boxing_config * config, const char * name_class, const char * name_property)
{
    return g_variant_to_uint(boxing_config_property((boxing_config *)config, name_class, name_property));
}

//----------------------------------------------------------------------------
/**!
  * Verify if name_property property of name_class class is set
  */
DBOOL boxing_config_is_set(const boxing_config * config, const char * name_class, const char * name_property)
{
    GHashTable * class_hash = g_hash_table_lookup(config->class_properties, name_class);
    if (class_hash != NULL)
    {
        return g_hash_table_lookup(class_hash, name_property) != NULL;
    }

    return DFALSE;
}

//----------------------------------------------------------------------------
/**!
  * Get nameProperties of name_class class
  */
void boxing_config_properties(boxing_config * config, const char * name_class, const GHashTable ** property_list)
{
    *property_list = g_hash_table_lookup(config->class_properties, name_class);
}


//----------------------------------------------------------------------------
/**!
  * Separe the properties in list whit separator comma
  * @p name_class The class name to separate
  * @p name_property The property name to separate
  */
gvector * boxing_config_parse_list_properties(boxing_config * config, const char * name_class, const char * name_property)
{
    char * data = g_variant_if_string(boxing_config_property(config, name_class, name_property));
    if (data != NULL)
    {
        gvector * list_separate = boxing_string_split(data, PROPERTIES_SEPARATOR);
        return list_separate;
    }
    else
    {
        return gvector_create(sizeof(char *), 0);
    }
}

//----------------------------------------------------------------------------
/**!
  * Get name_property property of name_class class as integer point.
  */
boxing_pointi boxing_config_property_pointi(boxing_config * config, const char * name_class, const char * name_property, DBOOL * was_found)
{
    boxing_pointi return_value;
    char * property_hash;
    GHashTable * class_hash = g_hash_table_lookup(config->class_properties, name_class);
    
    return_value.x = return_value.y = 0;
    if (class_hash != NULL) {
        property_hash = g_hash_table_lookup(class_hash, name_property);
        if (property_hash != NULL) {
            gvector * list_coord = boxing_config_parse_list_properties(config, name_class, name_property);
            if (list_coord->size != 2) {
                DLOG_ERROR1("PointF unsupported format %s", property_hash);
                *was_found = DFALSE;
                return return_value;
            }
            else {
                return_value.x = atoi(GVECTORN(list_coord, char *, 0));
                return_value.y = atoi(GVECTORN(list_coord, char *, 1));
            }
            gvector_free(list_coord);
        }
        else
        {
            *was_found = DFALSE;
            return return_value;
        }
    }
    else
    {
        *was_found = DFALSE;
        return return_value;
    }
    *was_found = DTRUE;
    return return_value;
}

//----------------------------------------------------------------------------
/**
  * Get name_property property of name_class class as point
  */
boxing_pointf boxing_config_property_pointf(boxing_config * config, const char * name_class, const char * name_property, DBOOL * was_found)
{
    char * property_hash;
    GHashTable * class_hash = g_hash_table_lookup(config->class_properties, name_class);
    boxing_pointf return_value;
    return_value.x = return_value.y = 0;
    if (class_hash != NULL) {
        property_hash = g_hash_table_lookup(class_hash, name_property);
        if (property_hash != NULL) {
            gvector * list_coord = boxing_config_parse_list_properties(config, name_class, name_property);
            if (list_coord->size != 2) {
                DLOG_ERROR1("PointF unsupported format %s", property_hash);
                *was_found = DFALSE;
                return return_value;
            }
            else {
                return_value.x = (boxing_float)atof(GVECTORN(list_coord, char *, 0));
                return_value.y = (boxing_float)atof(GVECTORN(list_coord, char *, 1));
            }
            gvector_free(list_coord);
        }
        else
        {
            *was_found = DFALSE;
            return return_value;
        }
    }
    else
    {
        *was_found = DFALSE;
        return return_value;
    }
    *was_found = DTRUE;
    return return_value;
}


//----------------------------------------------------------------------------
/*!
 *  Clone the config.
 */

boxing_config * boxing_config_clone(const boxing_config * config)
{
    if (config == NULL)
    {
        return NULL;
    }

    int i, j;
    boxing_config * copy = boxing_config_create();
    for (i = 0; i < config->class_properties->size; i++)
    {
        guint class_hash = config->class_properties->hashes[i];
        char * class_key = config->class_properties->keys[i];
        GHashTable * node_class = config->class_properties->values[i];
        
        if (HASH_IS_REAL (class_hash) )
        {
            GHashTable * copy_class = g_hash_table_new_full(g_str_hash, g_str_equal, boxing_utils_g_hash_table_destroy_item_string, boxing_utils_g_hash_table_destroy_item_g_variant);

            for (j = 0; j < node_class->size; j++)
            {
                guint node_hash = node_class->hashes[j];
                char * node_key = node_class->keys[j];
                g_variant * node_value = node_class->values[j];

                if (HASH_IS_REAL (node_hash) )
                {
                    g_hash_table_replace(copy_class, boxing_string_clone(node_key), g_variant_clone(node_value));
                }
            }
            g_hash_table_replace(copy->class_properties, boxing_string_clone(class_key), copy_class);
        }
    }

    for (i = 0; i < config->class_alias->size; i++)
    {
        guint node_hash = config->class_alias->hashes[i];
        char * node_key = config->class_alias->keys[i];
        char * node_value = config->class_alias->values[i];

        if (HASH_IS_REAL (node_hash) )
        {
            g_hash_table_replace(copy->class_alias, boxing_string_clone(node_key), boxing_string_clone(node_value));
        }
    }
    return copy;
}

//----------------------------------------------------------------------------
/**!
 * Compares two configs. Does not take into consideration assigned XML, only data and aliases.
 * \returns DTRUE if class_properties (data) and class_aliases (aliases) are equal.
 */
DBOOL boxing_config_is_equal(boxing_config *a, boxing_config *b)
{
    int i, j;
    if (a == NULL || b == NULL)
    {
        return a == b;
    }
    
    if (a->class_alias->size != b->class_alias->size || a->class_properties->size != b->class_properties->size)
    {
        return DFALSE;
    }

    // Check properties.
    for (i = 0; i < a->class_properties->size; i++)
    {
        if (!HASH_IS_REAL(a->class_properties->hashes[i]))
        {
            continue;
        }
        GHashTable * b_class = g_hash_table_lookup(b->class_properties, a->class_properties->keys[i]);
        if (b_class == NULL)
        {
            return DFALSE;
        }
        GHashTable * a_class = a->class_properties->values[i];

        if (a_class->size != b_class->size)
        {
            return DFALSE;
        }

        for (j = 0; j < a_class->size; j++)
        {
            if (!HASH_IS_REAL(a_class->hashes[j]))
            {
                continue;
            }
            g_variant * b_property = g_hash_table_lookup(b_class, a_class->keys[j]);
            if (b_property == NULL || !g_variant_equals(b_property, a_class->values[j]))
            {
                return DFALSE;
            }
        }
    }

    // Check aliases.
    for (i = 0; i < a->class_alias->size; i++)
    {
        if (!HASH_IS_REAL(a->class_alias->hashes[i]))
        {
            continue;
        }

        char * b_alias = g_hash_table_lookup(b->class_alias, a->class_alias->keys[i]);

        if (b_alias == NULL && a->class_alias->values[i] == NULL)
        {
            continue;
        }

        if (b_alias == NULL || !boxing_string_equal(b_alias, a->class_alias->values[i]))
        {
            return DFALSE;
        }
    }

    return DTRUE;
}
