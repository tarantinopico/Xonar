package com.tarantino.xonarx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tarantino.xonarx.domain.model.Userscript

@Entity(tableName = "userscripts")
data class UserscriptEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val domain: String?,
    val isEnabled: Boolean,
    val identityId: String,
    val isCss: Boolean
) {
    fun toDomainModel() = Userscript(id, name, code, domain, isEnabled, identityId, isCss)
    companion object {
        fun fromDomainModel(model: Userscript) = UserscriptEntity(
            model.id, model.name, model.code, model.domain, model.isEnabled, model.identityId, model.isCss
        )
    }
}
