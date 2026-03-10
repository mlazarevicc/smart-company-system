export const mapProductFromApi = (p) => ({
    id: p.id,
    sku: p.sku,
    name: p.name,
    description: p.description,
    category: p.category,
    price: p.price,
    weight: p.weight,
    version: p.version,
  
    productImage: p.product_image,
    isAvailable: p.is_available,
    isOnSale: p.is_on_sale,
  
    createdAt: p.created_at,
    updatedAt: p.updated_at,
  
    categoryName: p.category_name,
    displayPrice: p.display_price,

    factoryIds: p.factoryIds,
    factories: p.factories,

    totalQuantity: p.totalQuantity || 0
  });