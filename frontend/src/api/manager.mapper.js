export const mapManagerFromApi = (m) => ({
    id: m.id,
    username: m.username,
    email: m.email,
    role: m.role,
  
    firstName: m.first_name,
    lastName: m.last_name,
    displayName: m.display_name,
  
    profileImage: m.profile_image,
  
    isSuperManager: m.is_super_manager,
    isBlocked: m.is_blocked,
    isActive: m.is_active,
  
    status: m.status,
    createdAt: m.created_at,
  });