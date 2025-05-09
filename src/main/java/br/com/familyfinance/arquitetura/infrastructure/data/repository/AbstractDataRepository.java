package br.com.familyfinance.arquitetura.infrastructure.data.repository;

import br.com.familyfinance.arquitetura.domain.repository.BaseRepository;
import br.dev.paulocarvalho.arquitetura.application.exception.ApplicationErrorCodeEnum;
import br.dev.paulocarvalho.arquitetura.application.exception.ApplicationException;
import br.dev.paulocarvalho.arquitetura.domain.mapper.AbstractModelMapper;
import br.dev.paulocarvalho.arquitetura.domain.model.Model;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;

public abstract class AbstractDataRepository<MODEL extends Model<ID>, DATA, ID>
        implements PanacheRepositoryBase<DATA, ID>, BaseRepository<MODEL, ID> {

    protected abstract AbstractModelMapper<MODEL, DATA> getMapper();

    @Override
    public Uni<MODEL> buscarPorId(ID id) {
        return findById(id)
                .onItem()
                .transform(this.getMapper()::toModel);
    }

    @Transactional
    @Override
    public Uni<MODEL> inserir(MODEL model) {
        return persist(this.getMapper().toData(model))
                .onItem()
                .transform(this.getMapper()::toModel);
    }

    @Transactional
    @Override
    public Uni<MODEL> alterar(MODEL model) {
        return findById(model.getId())
                .onItem()
                .ifNull()
                .failWith(() -> new ApplicationException(ApplicationErrorCodeEnum.RECURSO_NAO_ENCONTRADO))
                .onItem()
                .ifNotNull()
                .transformToUni(data -> persist(this.getMapper().mergeData(model, data)))
                .onItem()
                .transform(this.getMapper()::toModel);
    }

    @Transactional
    @Override
    public Uni<Void> excluir(MODEL model) {
        return findById(model.getId())
                .onItem()
                .transformToUni(data -> delete(data));
    }

}
