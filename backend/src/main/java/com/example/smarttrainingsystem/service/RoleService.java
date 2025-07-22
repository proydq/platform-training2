package com.example.smarttrainingsystem.service;

import com.example.smarttrainingsystem.dto.RoleDTO;
import com.example.smarttrainingsystem.entity.Role;
import com.example.smarttrainingsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleDTO.Option> getAllRoles() {
        List<Role> roles = roleRepository.findAllActiveRoles();
        return roles.stream().map(r -> {
            RoleDTO.Option opt = new RoleDTO.Option();
            opt.setLabel(r.getRoleName());
            opt.setValue(r.getRoleCode());
            return opt;
        }).collect(Collectors.toList());
    }
}
