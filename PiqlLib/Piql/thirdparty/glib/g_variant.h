/*****************************************************************************
**
**  Definition of the g_variant interface
**
**  Creation date:  2015/08/17
**  Created by:     Piql AS
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**  Port of glib gvariant.
**
*****************************************************************************/

#ifndef G_VARIANT_H
#define G_VARIANT_H

#ifdef __cplusplus
extern "C" {
#endif

#include "boxing/bool.h"

#include "boxing/math/math.h"

typedef enum
{
    G_VARIANT_POINTER,
    G_VARIANT_INT,
    G_VARIANT_UINT,
    G_VARIANT_LONGLONG,
    G_VARIANT_ULONGLONG,
    G_VARIANT_FLOAT,
    G_VARIANT_DOUBLE,
    G_VARIANT_STRING,
    G_VARIANT_POINTI
} g_variant_type;

typedef struct
{
    g_variant_type type;
    void * data;
} g_variant;

g_variant * g_variant_create_pointer(void * value);
g_variant * g_variant_create_int(int value);
g_variant * g_variant_create_uint(unsigned int value);
g_variant * g_variant_create_longlong(long long value);
g_variant * g_variant_create_ulonglong(unsigned long long value);
g_variant * g_variant_create_float(float value);
g_variant * g_variant_create_double(double value);
g_variant * g_variant_create_string(const char * value);
g_variant * g_variant_create_pointi(boxing_pointi point);
g_variant * g_variant_create(const char * value);
g_variant * g_variant_clone(const g_variant * variant);

void                g_variant_free(g_variant * variant);

void                g_variant_set_uint(g_variant * variant, unsigned int value);

int                 g_variant_to_int(const g_variant * variant);
unsigned int        g_variant_to_uint(const g_variant * variant);
long long           g_variant_to_longlong(const g_variant * variant);
unsigned long long  g_variant_to_ulonglong(const g_variant * variant);
int                 g_variant_to_bool(const g_variant * variant); // 1 - true, 0 - false
char *              g_variant_to_string(const g_variant * variant);// String is allocated here and the caller is responsible for freeing
boxing_pointi       g_variant_to_pointi(const g_variant * variant, DBOOL * success); // you can pass NULL as success
char *              g_variant_if_string(const g_variant * variant);// String is NOT allocated here, the existing string is returned IF the variant is a string.
DBOOL               g_variant_equals(const g_variant *a, const g_variant *b);

#ifdef __cplusplus
}
#endif


#endif
