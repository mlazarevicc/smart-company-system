package ftn.siit.nvt.security;

import ftn.siit.nvt.repository.CustomerRepository;
import ftn.siit.nvt.repository.ManagerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository, ManagerRepository managerRepository) {
        this.customerRepository = customerRepository;
        this.managerRepository = managerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            return customer.get();
        }

        var manager = managerRepository.findByEmail(email);
        if (manager.isPresent()) {
            return manager.get();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}