package xyz.dcln.androidutils.utils.ext

import xyz.dcln.androidutils.utils.PermissionUtils
import xyz.dcln.androidutils.utils.RegexUtils

/**
 * Created by dcl on 2023/8/14.
 */

// String的扩展函数，

// 查看注释请到RegexUtils
fun String.isValidEmail() = RegexUtils.isValidEmail(this)
fun String.isValidUrl() = RegexUtils.isValidUrl(this)
fun String.containsChineseCharacters() = RegexUtils.containsChineseCharacters(this)
fun String.isValidDate() = RegexUtils.isValidDate(this)
fun String.isValidIpAddress() = RegexUtils.isValidIpAddress(this)
fun String.hasDoubleByteCharacters() = RegexUtils.hasDoubleByteCharacters(this)
fun String.isValidInteger() = RegexUtils.isValidInteger(this)
fun String.isValidPositiveInteger() = RegexUtils.isValidPositiveInteger(this)
fun String.isValidFloat() = RegexUtils.isValidFloat(this)
fun String.isValidPositiveFloat() = RegexUtils.isValidPositiveFloat(this)
fun String.isSymbol() = RegexUtils.isSymbol(this)
fun String.isDigit() = RegexUtils.isDigit(this)
fun String.isLetter() = RegexUtils.isLetter(this)


//String为Permission,是否有该权限
fun String.isPermissionGranted(): Boolean =PermissionUtils.hasPermission(this)
