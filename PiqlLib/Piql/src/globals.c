/*****************************************************************************
**
**  Boxinglib global definitions
**
**  Creation date:  2015/10/24
**  Created by:     Piql AS
**
**
**  Copyright (c) 2015 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

//  PROJECT INCLUDES
//

#include "boxing/globals.h"

//  CONSTANTS
//

/**
  * Node name for class node. All node names are lowercase
  */
const char * CONFIG_XML_NODE_CLASS = "class";
/**
  * Node name for property node. All attribute names are lowercase
  */
const char * CONFIG_XML_NODE_PROPERTY = "property";
/**
  * Name for attribute attribute. All attribute names are lowercase
  */
const char * CONFIG_XML_ATTR_NAME = "key";
/**
  * Name for value attribute. All attribute names are lowercase
  */
const char * CONFIG_XML_ATTR_VALUE = "value";
/**
  * Name for key alias.
  */
const char * CONFIG_XML_KEY_ALIAS = "alias";
/**
  * Name for version attribute. All attribute names are lowercase
  */
const char * CONFIG_XML_ATTR_VERSION = "version";



const char * PROPERTIES_SEPARATOR              = ",";
const char * FORWARD_ERROR_CORRECTION_PROCESS  = "process";

/**
  * Frame and DBox Info
  */
const char * FRAME_BUILDER_BLOCKSIZE               = "blockSize"; // deprecated, use FRAME_BUILDER_SCALINGFACTOR instead
const char * FRAME_BUILDER_SCALINGFACTOR           = "scalingFactor";
const char * FRAME_BUILDER_COLORDEPTH              = "colorDepth";
const char * FRAME_BUILDER_PRINTCOLORDEPTH         = "printColorDepth";
const char * FRAME_BUILDER_COMPONENTMASK           = "componentMask";
const char * FRAME_BUILDER_FRAMENUMBER             = "frameNumber";
const char * IMAGERAW_RAW_VERSION                  = "rawVersion";

