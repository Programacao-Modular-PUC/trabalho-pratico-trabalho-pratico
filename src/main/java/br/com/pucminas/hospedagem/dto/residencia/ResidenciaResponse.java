package br.com.pucminas.hospedagem.dto.residencia;

import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;

import java.util.List;

/**
 * Dados de saida de uma residencia, incluindo um resumo dos seus quartos.
 */
public record ResidenciaResponse(
        Long id,
        String endereco,
        String numero,
        String bairro,
        String cep,
        String telefone,
        String email,
        List<QuartoResumoResponse> quartos
) {

    /** Converte uma entidade {@link Residencia} em sua representacao de saida. */
    public static ResidenciaResponse fromEntity(Residencia residencia) {
        List<Quarto> quartos = residencia.getQuartos();
        List<QuartoResumoResponse> quartosResumo = quartos == null
                ? List.of()
                : quartos.stream()
                        .map(QuartoResumoResponse::fromEntity)
                        .toList();
        return new ResidenciaResponse(
                residencia.getId(),
                residencia.getEndereco(),
                residencia.getNumero(),
                residencia.getBairro(),
                residencia.getCep(),
                residencia.getTelefone(),
                residencia.getEmail(),
                quartosResumo
        );
    }
}
