package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

//@Component - não especifico
//@Repository
@Service
public class CategoryService {
	@Autowired
	private CategoryRepository repository;

	@Transactional(readOnly = true)
	public List<CategoryDTO> findAll() {
		List<Category> list = repository.findAll();
		return list.stream().map(x -> new CategoryDTO(x)).collect(Collectors.toList());

	}

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> obj = repository.findById(id);
		Category entity = obj.orElseThrow(() -> new ResourceNotFoundException("Id não existente!"));
		// obtém o Category dentro do Optional, se não existir retorna um erro
		// instanciando a exceção criada

		return new CategoryDTO(entity); // retorna o DTO e não a entidade - recebendo entity como argumento.
		// Optional evita trabalhar com valor null
	}

	@Transactional
	public CategoryDTO insert(CategoryDTO dto) {
		Category entity = new Category();
		entity.setName(dto.getName());
		entity = repository.save(entity);
		return new CategoryDTO(entity);
		// converter para uma entidade category
	}

	@Transactional // atualizar um registro na JPA - usar getOne ou Spring recentes:
					// getReferenceById
	public CategoryDTO update(Long id, CategoryDTO dto) {
		try {
			Category entity = repository.getOne(id); // está instanciado
			entity.setName(dto.getName()); // atualizei os dados da entidade que estava na memória
			entity = repository.save(entity);
			return new CategoryDTO(entity);
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
