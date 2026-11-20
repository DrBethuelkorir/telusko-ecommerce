    package com.kiptoo.e_com_tel.models;
    
    import com.fasterxml.jackson.annotation.JsonFormat;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import java.math.BigDecimal;
    import java.util.Date;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    public class Products {
    
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        private Date expirationDate;
        private boolean available;
        private Long quantity;

        @ManyToOne
        @JoinColumn(name = "user_id") 
        private Users user;
    }
