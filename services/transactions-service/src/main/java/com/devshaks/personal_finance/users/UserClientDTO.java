package com.devshaks.personal_finance.users;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserClientDTO {
    private Long id;
    private String firstname;
    private String email;
}
