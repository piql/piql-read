/*****************************************************************************
**
**  Storing the description of statistical structures
**
**  Creation date:  2017/07/12
**  Created by:     Oleksandr Ivanov
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//----------------------------------------------------------------------------
/*!
 *  \struct  boxing_stats_decode_s  stats.h
 *  \brief   Boxing decode statistic
 *  \ingroup unbox
 *
 *  \var resolved_errors         Number of resolver boxing errors. 
 *  \var unresolved_errors       Number of unresolver boxing errors.
 *  \var fec_accumulated_amount  FEC accumulated amount.
 *  \var fec_accumulated_weight  FEC accumulated weight.
 *
 *  The struct with statistic information about boxing process.
 */


//----------------------------------------------------------------------------
/*!
 *  \struct  boxing_stats_mtf_s  stats.h
 *  \brief   Boxing decode mtf statistic 
 *  \ingroup unbox
 *
 *  \var top_horizontal_mtf     Top horizontal mtf.
 *  \var bottom_horizontal_mtf  Bottom horizontal mtf.
 *  \var left_vertical_mtf      Left vertical mtf.
 *  \var right_vertical_mtf     Right vertical mtf.
 *
 *  The struct with mtf statistic information about boxing process.
 */


//----------------------------------------------------------------------------
/*!
 *  \struct  boxing_reference_bar_stats_s  stats.h
 *  \brief   Boxing decode reference bar statistic 
 *  \ingroup unbox
 *
 *  \var top_mtf       Top mtf.
 *  \var bottom_mtf    Bottom mtf.
 *  \var left_mtf      Left mtf.
 *  \var right_mtf     Right mtf.
 *
 *  The struct with reference bar mtf statistic information about boxing process.
 */

static void just_empty_function(){} // Empty function to avoid warnings - ISO C requires a translation unit to contain at least one declaration
