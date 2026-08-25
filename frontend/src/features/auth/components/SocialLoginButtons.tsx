import { Github, Mail } from "lucide-react";
import { Button } from "../../../components/ui/button";

const providers = [
  { label: "Google", icon: Mail },
  { label: "GitHub", icon: Github }
];

export function SocialLoginButtons() {
  return (
    <div className="grid gap-3 sm:grid-cols-2">
      {providers.map((provider) => (
        <Button key={provider.label} variant="secondary" className="h-10" disabled>
          <provider.icon size={16} />
          {provider.label}
        </Button>
      ))}
    </div>
  );
}
