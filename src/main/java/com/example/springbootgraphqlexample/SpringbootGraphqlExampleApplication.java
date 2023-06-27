package com.example.springbootgraphqlexample;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@SpringBootApplication
public class SpringbootGraphqlExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootGraphqlExampleApplication.class, args);
	}


	/*@Bean
	RuntimeWiringConfigurer runtimeWiringConfigurer(CrmService crmService) {
		return builder -> {
			builder.type("Customer",
					wiring -> wiring
							.dataFetcher("profile",
									env -> crmService.getCustomerProfile(env.getSource())));
			builder.type("Query",
					wiring -> wiring
							.dataFetcher("customerById",
									environment -> crmService.getCustomerById(
											Integer.parseInt(environment.getArgument("id"))))
							.dataFetcher("customers", environment -> crmService.getCustomers()));
		};
	}*/
}

@Controller
class GraphqlController {

	private CrmService crmService;
	GraphqlController(CrmService crmService) {
		this.crmService = crmService;
	}

	@QueryMapping
	Flux<Customer> customers() {
		System.out.println("fetching customers");
		return Flux.fromIterable(crmService.getCustomers());
	}

	@BatchMapping
	Map<Customer, Profile> profile(List<Customer> customers) {
		System.out.println("fetching profiles for "+ customers.size()+ " customers");
		return customers
				.stream()
				.collect(Collectors.toMap(Function.identity(), cust -> new Profile(cust.id(), cust.id())));
	}

	// N+1 Problem : Solution use BatchMapping
	/*@SchemaMapping(typeName = "Customer", field = "profile")
	Profile getCustomerProfile(Customer customer) {
		System.out.println("fetching profiles for customer: "+ customer.id());
		return crmService.getCustomerProfile(customer);
	}*/
}

record Profile(Integer id, Integer customerId){}

record Customer(Integer id, String name){}

@Service
class CrmService {

	Profile getCustomerProfile(Customer customer) {
		return new Profile(customer.id(), customer.id());
	}

	List<Customer> getCustomers() {
		return List.of(new Customer(1, "CustA"),
				new Customer(2, "CustB"));
	}

	Customer getCustomerById(Integer id) {
		return new Customer(id, Math.random() > 0.5 ? "A": "B");
	}
}
