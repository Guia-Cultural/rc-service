package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Usuarios.UserReq;
import com.rotacultural.rcservice.DTOs.Usuarios.UserResp;
import com.rotacultural.rcservice.entities.Usuarios;
import com.rotacultural.rcservice.repositories.UsuariosRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuariosService {
    private final UsuariosRepository usuariosRepository;

    public UsuariosService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    public UserResp create(UserReq userReq) {
        Usuarios usuario = new Usuarios();
        usuario.setNome(userReq.getNome());
        usuario.setEmail(userReq.getEmail());
        usuario.setSenha(userReq.getSenha());
        if(userReq.getCep() != null && !userReq.getCep().isEmpty()){
            usuario.setCep(userReq.getCep());
        }
        usuario.setRole(userReq.getRole());
        if (userReq.getBio() != null && !userReq.getBio().isEmpty()){
            usuario.setBio(userReq.getBio());
        }
        usuariosRepository.save(usuario);

        UserResp userResp = new UserResp();
        userResp.setId(userResp.getId());
        userResp.setNome(userReq.getNome());
        userResp.setEmail(userReq.getEmail());
        userResp.setCep(userReq.getCep());
        userResp.setBio(userReq.getBio());
        userResp.setRole(userReq.getRole());
        userResp.setDataCriacao(usuario.getDataCriacao());
        return userResp;
    }

    public List<UserResp> getAll() {
        List<Usuarios> usuarios = usuariosRepository.findAll();
        List<UserResp> userResps = new ArrayList<>();
        for (Usuarios usuario : usuarios) {
            UserResp userResp = new UserResp();
            userResp.setId(usuario.getIdUser());
            userResp.setNome(usuario.getNome());
            userResp.setEmail(usuario.getEmail());
            userResp.setCep(usuario.getCep());
            userResp.setBio(usuario.getBio());
            userResp.setRole(usuario.getRole());
            userResps.add(userResp);
        }

        return userResps;
    }

    public UserResp getById(long id) {
        Usuarios usuario = usuariosRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        UserResp userResp = new UserResp();
        userResp.setId(usuario.getIdUser());
        userResp.setNome(usuario.getNome());
        userResp.setEmail(usuario.getEmail());
        userResp.setCep(usuario.getCep());
        userResp.setBio(usuario.getBio());
        userResp.setRole(usuario.getRole());
        return userResp;
    }

    public List<UserResp> getByCep(String cep) {
        List<Usuarios> usuarios = usuariosRepository.findByCep(cep);
        if(usuarios.isEmpty()){
            throw new EntityNotFoundException("Cep não encontrado");
        }
        List<UserResp> userResps = new ArrayList<>();
        for (Usuarios usuario : usuarios) {
            UserResp userResp = new UserResp();
            userResp.setId(usuario.getIdUser());
            userResp.setNome(usuario.getNome());
            userResp.setEmail(usuario.getEmail());
            userResp.setCep(usuario.getCep());
            userResp.setBio(usuario.getBio());
            userResp.setRole(usuario.getRole());
            userResps.add(userResp);
        }
        return userResps;
    }

    public UserResp update(long id, UserReq userReq) {
        Usuarios usuario = usuariosRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        usuario.setNome(userReq.getNome());
        usuario.setEmail(userReq.getEmail());
        usuario.setCep(userReq.getCep());
        usuario.setBio(userReq.getBio());
        usuario.setRole(userReq.getRole());
        usuariosRepository.save(usuario);
        UserResp userResp = new UserResp();
        userResp.setId(usuario.getIdUser());
        userResp.setNome(usuario.getNome());
        userResp.setEmail(usuario.getEmail());
        userResp.setCep(usuario.getCep());
        userResp.setBio(usuario.getBio());
        userResp.setRole(usuario.getRole());
        userResp.setDataCriacao(usuario.getDataCriacao());
        return userResp;
    }

    public void delete(long id) {
        if (usuariosRepository.existsById(id)) {
            usuariosRepository.deleteById(id);
        }else {
            throw new EntityNotFoundException("Usuario não encontrado");
        }
    }
}
