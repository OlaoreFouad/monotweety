package net.yslibrary.monotweety.appdata.local

import com.pushtorefresh.storio3.sqlite.operations.delete.PreparedDelete
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGet
import com.pushtorefresh.storio3.sqlite.operations.put.PreparedPut

fun <T> PreparedPut.Builder.withObject(entity: T) = `object`(entity)

fun <T> PreparedPut.Builder.withObjects(entities: Collection<T>) = objects(entities)

fun <T> PreparedGet.Builder.singleObject(entityClass: Class<T>) = `object`(entityClass)

fun <T> PreparedDelete.Builder.withObject(entity: T) = `object`(entity)

fun <T> PreparedDelete.Builder.withObjects(entities: Collection<T>) = objects(entities)