#ifndef CONFIGC_H
#define CONFIGC_H

/*****************************************************************************
**
**  Definition of the config interface
**
**  Creation date:  2014/12/16
**  Created by:     Piql AS
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

#ifdef __cplusplus
extern "C" {
#endif

#include "ghash.h"
#include "g_variant.h"
#include "gvector.h"
#include "boxing/utils.h"
#include "boxing/string.h"
#include "boxing/bool.h"


typedef struct boxing_config_s
{
    GHashTable * class_properties; // <string, map<string, variant> >
    GHashTable * class_alias; // <string, string>
} boxing_config;


boxing_config *   boxing_config_create();
void              boxing_config_free(boxing_config * config);
boxing_config *   boxing_config_clone(const boxing_config * config);
DBOOL             boxing_config_is_equal(boxing_config *a, boxing_config *b);
boxing_config *   boxing_config_instance();
void              boxing_config_set_property(boxing_config * config, const char * name_class, const char * name_property, const char * property_value);
void              boxing_config_set_property_uint(boxing_config * config, const char * name_class, const char * name_property, unsigned int property_value);


g_variant *       boxing_config_property(boxing_config * config, const char * name_class, const char * name_property);
const g_variant * boxing_config_property_const(const boxing_config * config, const char * name_class, const char * name_property);
unsigned int      boxing_config_property_uint(const boxing_config * config, const char * name_class, const char * name_property);
void              boxing_config_properties(boxing_config * config, const char * name_class, const GHashTable ** /*map<string, variant>*/ property_list);
boxing_pointi     boxing_config_property_pointi(boxing_config * config, const char * name_class, const char * name_property, DBOOL * was_found);
boxing_pointf     boxing_config_property_pointf(boxing_config * config, const char * name_class, const char * name_property, DBOOL * was_found);
DBOOL             boxing_config_is_set(const boxing_config * config, const char * name_class, const char * name_property);
gvector *         boxing_config_parse_list_properties(boxing_config * config, const char * name_class, const char * name_property);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif
