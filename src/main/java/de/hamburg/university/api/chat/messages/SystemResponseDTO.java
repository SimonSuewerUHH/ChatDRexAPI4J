package de.hamburg.university.api.chat.messages;


import de.hamburg.university.api.base.BaseDTO;
import de.hamburg.university.api.network.DrugstOneNetworkDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemResponseDTO extends BaseDTO {

    private DrugstOneNetworkDTO drugstOneNetwork;

    @URL
    private String url;
}