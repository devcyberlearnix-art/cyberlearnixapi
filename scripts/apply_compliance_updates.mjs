import fs from 'fs';
import path from 'path';

const uiRoot = 'C:/Users/SHIVASAI/OneDrive/Desktop/FD_LMS/UI';

// 1. Update CourseDetail.jsx
const courseDetailPath = path.join(uiRoot, 'src/pages/CourseDetail.jsx');
if (fs.existsSync(courseDetailPath)) {
  let cd = fs.readFileSync(courseDetailPath, 'utf8');
  cd = cd.replace(/\{\`\$\$\{course\.price\}\`\}/g, "{`₹${course.price}`}");
  cd = cd.replace(/\?\{course\.originalPrice\}/g, "₹{course.originalPrice}");
  cd = cd.replace(/\$\{course\.originalPrice\}/g, "₹{course.originalPrice}");
  fs.writeFileSync(courseDetailPath, cd, 'utf8');
  console.log('✓ Standardized currency in CourseDetail.jsx to ₹ (INR)');
}

// 2. Update Subscription.jsx
const subPath = path.join(uiRoot, 'src/pages/Subscription.jsx');
if (fs.existsSync(subPath)) {
  let sub = fs.readFileSync(subPath, 'utf8');
  sub = sub.replace('price: "$9"', 'price: "₹749"');
  sub = sub.replace('price: "$19"', 'price: "₹1,499"');
  sub = sub.replace('price: "$29"', 'price: "₹2,499"');
  fs.writeFileSync(subPath, sub, 'utf8');
  console.log('✓ Standardized currency in Subscription.jsx to ₹ (INR)');
}

// 3. Align TanStack Query Keys in useStudentData.js
const studentDataPath = path.join(uiRoot, 'src/hooks/useStudentData.js');
if (fs.existsSync(studentDataPath)) {
  let sd = fs.readFileSync(studentDataPath, 'utf8');
  
  // Update useStudentData to use user?.id || 'guest'
  sd = sd.replace(
    /queryKey:\s*\['student',\s*'dashboard'\],/g,
    "queryKey: ['student', 'dashboard', user?.id || 'guest'],"
  );
  sd = sd.replace(
    /queryKey:\s*\['course',\s*'progress',\s*courseId\],/g,
    "queryKey: ['course', 'progress', user?.id || 'guest', courseId],"
  );
  
  // Ensure useStudentData extracts user
  sd = sd.replace(
    /export const useStudentData = \(courseId = null\) => \{\s*const queryClient = useQueryClient\(\);/g,
    `export const useStudentData = (courseId = null) => {
  const queryClient = useQueryClient();
  const { user } = useSafeAuth();`
  );

  // Update useStudentWishlist queryKey
  sd = sd.replace(
    /export const useStudentWishlist = \(\) => \{\s*const queryClient = useQueryClient\(\);/g,
    `export const useStudentWishlist = () => {
  const queryClient = useQueryClient();
  const { user } = useSafeAuth();
  const userId = user?.id || 'guest';`
  );

  sd = sd.replace(
    /queryKey:\s*\['wishlist'\],/g,
    "queryKey: ['wishlist', userId],"
  );

  sd = sd.replace(
    /queryClient\.cancelQueries\(\{\s*queryKey:\s*\['wishlist'\]\s*\}\);/g,
    "queryClient.cancelQueries({ queryKey: ['wishlist', userId] });"
  );

  sd = sd.replace(
    /queryClient\.getQueryData\(\['wishlist'\]\)/g,
    "queryClient.getQueryData(['wishlist', userId])"
  );

  sd = sd.replace(
    /queryClient\.invalidateQueries\(\{\s*queryKey:\s*\['wishlist'\]\s*\}\);/g,
    "queryClient.invalidateQueries({ queryKey: ['wishlist', userId] });"
  );

  sd = sd.replace(
    /queryKey:\s*\['wishlist-check',\s*courseId\],/g,
    "queryKey: ['wishlist-check', userId, courseId],"
  );

  sd = sd.replace(
    /queryClient\.cancelQueries\(\{\s*queryKey:\s*\['wishlist-check',\s*courseId\]\s*\}\);/g,
    "queryClient.cancelQueries({ queryKey: ['wishlist-check', userId, courseId] });"
  );

  sd = sd.replace(
    /queryClient\.invalidateQueries\(\{\s*queryKey:\s*\['wishlist-check',\s*courseId\]\s*\}\);/g,
    "queryClient.invalidateQueries({ queryKey: ['wishlist-check', userId, courseId] });"
  );

  fs.writeFileSync(studentDataPath, sd, 'utf8');
  console.log('✓ Aligned TanStack Query key structures in useStudentData.js');
}

console.log('Compliance updates completed.');
