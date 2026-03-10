export const mapCompanyFromApi = (c) => ({
    id: c.id,
    status: c.status,
    name: c.name,
    address: c.address,
    city: c.city,
    country: c.country,
    latitude: c.latitude,
    longitude: c.longitude,
    images: c.images,
    proofOfOwnershipUrls: c.proofOfOwnershipUrls,
    ownerName: c.ownerName
})