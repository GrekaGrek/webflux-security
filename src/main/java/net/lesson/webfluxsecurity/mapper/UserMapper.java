package net.lesson.webfluxsecurity.mapper;

import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.model.UserDTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO map(UserEntity entity);

    @InheritInverseConfiguration
    UserEntity map(UserDTO item);
}
