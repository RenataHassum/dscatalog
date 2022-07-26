package com.devsuperior.dscatalog.services;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

//@Component - não especifico
//@Repository
@Service
public class ProductService {
	@Autowired
	private ProductRepository repository;

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> list = repository.findAll(pageRequest);
		return list.map(x -> new ProductDTO(x));
		//return list.stream().map(x -> new ProductDTO(x)).collect(Collectors.toList());
		// Page já é um stream
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Id não existente!"));
		// obtém o Product dentro do Optional, se não existir retorna um erro
		// instanciando a exceção criada

		return new ProductDTO(entity, entity.getCategories()); // retorna o DTO e não a entidade - recebendo entity como argumento.
		// Optional evita trabalhar com valor null
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		//entity.setName(dto.getName());
		entity = repository.save(entity);
		return new ProductDTO(entity);
		// converter para uma entidade category
	}

	@Transactional // atualizar um registro na JPA - usar getOne ou Spring recentes:
					// getReferenceById
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getOne(id); // está instanciado
			//entity.setName(dto.getName()); // atualizei os dados da entidade que estava na memória
			entity = repository.save(entity);
			return new ProductDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id não encontrado" + id);
		}

		// findById - vai no banco de dados e trás os dados
		// getOne não vai no banco, ele instancia um objeto provisório com o id, qdo
		// salvar ele vai no banco
	}
	
	// não coloca o @Transactional - delete vai capturar uma exceção do banco de dados o transactional não deixa capturar
	public void delete(Long id) {
		try {
		repository.deleteById(id);
		}
		catch(EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id não encontrado!");
		}
		catch(DataIntegrityViolationException e) { // para não deletar categoria e os produtos ficarem sem categoria
			throw new DatabaseException("Violçao de integridade");
		}
	}

}
