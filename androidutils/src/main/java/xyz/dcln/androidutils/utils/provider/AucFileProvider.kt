package xyz.dcln.androidutils.utils.provider

import androidx.core.content.FileProvider

class AucFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        return true
    }
}