package com.yucsan.mapgendafernandochang2025.mapper

import com.yucsan.mapgendafernandochang2025.dto.UsuarioDTO
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity

fun UsuarioDTO.toEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = id.toString(),
        nombre = nombre,
        apellido = apellido,
        email = email,
        telefono = telefono,
        rol = rol,
        pais = pais,
        ciudad = ciudad,
        direccion = direccion,
        descripcion = descripcion,
        fotoPerfilUri = fotoPerfilUrl,
        verificado = verificado,
        sincronizado = true
    )
}



fun UsuarioEntity.toDTO(): UsuarioDTO {
    return UsuarioDTO(
        id = java.util.UUID.fromString(id),
        nombre = nombre,
        apellido = apellido,
        email = email,
        telefono = telefono,
        rol = rol,
        pais = pais,
        ciudad = ciudad,
        direccion = direccion,
        descripcion = descripcion,
        verificado = verificado,
        fotoPerfilUrl = fotoPerfilUri // asegúrate de que UsuarioDTO tenga este campo
    )
}
