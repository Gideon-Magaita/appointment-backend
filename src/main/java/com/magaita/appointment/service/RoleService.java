package com.magaita.appointment.service;

import com.magaita.appointment.dto.RoleDto;
import com.magaita.appointment.res.Response;

import java.util.List;

public interface RoleService {

    Response<RoleDto>createRole(RoleDto roleDto);

    Response<RoleDto>updateRole(RoleDto roleRequest);

    Response<RoleDto>getRoleById(Long id);

    Response<List<RoleDto>>getAllRole();

    Response<?>deleteRole(Long id);
}
